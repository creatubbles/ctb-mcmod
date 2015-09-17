package com.creatubbles.repack.endercore.common.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

import org.apache.commons.lang3.StringUtils;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.repack.endercore.common.config.annot.Comment;
import com.creatubbles.repack.endercore.common.config.annot.Config;
import com.creatubbles.repack.endercore.common.config.annot.NoSync;
import com.creatubbles.repack.endercore.common.config.annot.Range;
import com.creatubbles.repack.endercore.common.config.annot.RestartReq;
import com.creatubbles.repack.enderlib.common.Lang;
import com.creatubbles.repack.enderlib.common.util.Bound;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

/**
 * This class can be used to automatically process {@link Config} annotations on fields, and sync the data in those fields to clients. It will also automatically respond to all config changed events
 * and handle them appropriately.
 * 
 * @see #process(boolean)
 */
public class ConfigProcessor {

	public interface IReloadCallback {

		void callback(ConfigProcessor inst);
	}

	/**
	 * A simple adapter for reading custom config types.
	 *
	 * @param <BASE>
	 *            Must be a possible class that can be used in configs. This consists of:
	 *            <ul>
	 *            <code>
	 * <li>Boolean</li>
	 * <li>Integer</li>
	 * <li>Double</li>
	 * <li>String</li>
	 * <li>boolean[]</li>
	 * <li>int[]</li>
	 * <li>double[]</li>
	 * <li>String[]</li>
	 * </code>
	 *            </ul>
	 * @param <ACTUAL>
	 *            The actual type of this adapter. This is what the field type must be for this adapter to be applied.
	 */
	public interface ITypeAdapter<ACTUAL, BASE> {

		TypeToken<ACTUAL> getActualType();

		Property.Type getType();

		/**
		 * If this binds to a primitive type, return it here (e.g. int.class). Otherwise, return null.
		 * 
		 * @return The class for this ITypeAdapter's primitive type.
		 */
		@Nullable
		Class<?> getPrimitiveType();

		ACTUAL createActualType(BASE base);

		BASE createBaseType(ACTUAL actual);
	}

	static final Map<String, ConfigProcessor> processorMap = Maps.newHashMap();

	private final List<ITypeAdapter<?, ?>> adapters = Lists.newArrayList();

	final String modid;

	private final Class<?> configs;
	private final Configuration configFile;
	private final IReloadCallback callback;

	Map<String, Object> configValues = Maps.newHashMap();
	Map<String, Object> defaultValues = Maps.newHashMap();
	Map<String, Object> originalValues = Maps.newHashMap();

	private Set<String> sections = Sets.newHashSet();

	/**
	 * This constructor omits the callback arg.
	 * 
	 * @see #ConfigProcessor(Class, File, String, IReloadCallback)
	 */
	public ConfigProcessor(Class<?> configs, File configFile, String modid) {
		this(configs, configFile, modid, null);
	}

	/**
	 * Constructs a new ConfigProcessor to read and set {@link Config} values.
	 * 
	 * @param configs
	 *            The class which contains your {@link Config} annotations
	 * @param configFile
	 *            The file to use as the configuration file
	 * @param modid
	 *            The modid of the owner mod
	 * @param callback
	 *            an {@link IReloadCallback} object which will be called whenever config values are edited.
	 */
	public ConfigProcessor(Class<?> configs, File configFile, String modid, IReloadCallback callback) {
		this(configs, new Configuration(configFile), modid, callback);
	}

	private ConfigProcessor(Class<?> configs, Configuration configFile, String modid, IReloadCallback callback) {
		this.configs = configs;
		this.configFile = configFile;
		this.modid = modid;
		this.callback = callback;
		processorMap.put(modid, this);
		FMLCommonHandler.instance().bus().register(this);
		adapters.addAll(TypeAdapterBase.all);
	}

	public <ACTUAL, BASE> ConfigProcessor addAdapter(ITypeAdapter<ACTUAL, BASE> adapter) {
		adapters.add(adapter);
		return this;
	}

	public <ACTUAL, BASE> ConfigProcessor addAdapters(ITypeAdapter<ACTUAL, BASE>... adapters) {
		for (ITypeAdapter<ACTUAL, BASE> adapter : adapters) {
			addAdapter(adapter);
		}
		return this;
	}

	/**
	 * Processes all the configs in this processors class, optionally loading them from file first.
	 * 
	 * @param load
	 *            If true, the values from the file will be loaded. Otherwise, the values existing in memory will be used.
	 */
	public void process(boolean load) {
		if (load) {
			configFile.load();
		}

		try {
			for (Field f : configs.getDeclaredFields()) {
				processField(f);
			}
			if (callback != null) {
				callback.callback(this);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		configFile.save();
	}

	// returns true if the config value changed
	private boolean processField(Field f) throws Exception {
		Config cfg = f.getAnnotation(Config.class);
		if (cfg == null) {
			return false;
		}
		String name = f.getName();
		Object value = defaultValues.get(name);
		if (value == null) {
			value = f.get(null);
			defaultValues.put(name, value);
		}

		Object newValue = getConfigValue(cfg.value(), getComment(f), f, value);

		configValues.put(f.getName(), newValue);
		originalValues.put(f.getName(), newValue);
		f.set(null, newValue);

		sections.add(cfg.value());

		return !value.equals(newValue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getConfigValue(String section, String[] commentLines, Field f, Object defVal) {
		Property prop = null;
		Object res = null;
		Bound<Double> bound = getBound(f);
		ITypeAdapter adapter = getAdapterFor(f);
		String comment = StringUtils.join(commentLines, "\n");
		if (adapter != null) {
			defVal = adapter.createBaseType(defVal);
			switch (adapter.getType()) {
			case BOOLEAN:
				if (defVal.getClass().isArray()) {
					prop = configFile.get(section, f.getName(), (boolean[]) defVal, comment);
					res = prop.getBooleanList();
				} else {
					prop = configFile.get(section, f.getName(), (Boolean) defVal, comment);
					res = prop.getBoolean();
				}
				break;
			case DOUBLE:
				if (defVal.getClass().isArray()) {
					prop = configFile.get(section, f.getName(), (double[]) defVal, comment);
					res = boundDoubleArr(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue()));
				} else {
					prop = configFile.get(section, f.getName(), (Double) defVal, comment);
					res = boundValue(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue()), (Double) defVal);
				}
				break;
			case INTEGER:
				if (defVal.getClass().isArray()) {
					prop = configFile.get(section, f.getName(), (int[]) defVal, comment);
					res = boundIntArr(prop, Bound.of(bound.min.intValue(), bound.max.intValue()));
				} else {
					prop = configFile.get(section, f.getName(), (Integer) defVal, comment);
					res = boundValue(prop, Bound.of(bound.min.intValue(), bound.max.intValue()), (Integer) defVal);
				}
				break;
			case STRING:
				if (defVal.getClass().isArray()) {
					prop = configFile.get(section, f.getName(), (String[]) defVal, comment);
					res = prop.getStringList();
				} else {
					prop = configFile.get(section, f.getName(), (String) defVal, comment);
					res = prop.getString();
				}
				break;
			default:
				break;
			}
			if (res != null) {
				setBounds(prop, bound);
				addCommentDetails(prop, bound);
				getRestartReq(f).apply(prop);
				return adapter.createActualType(res);
			}
		}
		throw new IllegalArgumentException(String.format("No adapter for type %s in class %s, field %s", f.getGenericType(), configs, f));
	}

	private void setBounds(Property prop, Bound<?> bound) throws IllegalArgumentException {
		if (bound.equals(Bound.MAX_BOUND)) {
			return;
		}
		if (prop.getType() == Type.INTEGER) {
			Bound<Integer> b = Bound.of(bound.min.intValue(), bound.max.intValue());
			prop.setMinValue(b.min);
			prop.setMaxValue(b.max);
		} else if (prop.getType() == Type.DOUBLE) {
			Bound<Double> b = Bound.of(bound.min.doubleValue(), bound.max.doubleValue());
			prop.setMinValue(b.min);
			prop.setMaxValue(b.max);
		} else {
			throw new IllegalArgumentException(String.format("A mod tried to set bounds %s on a property that was not either of Integer of Double type.", bound));
		}
	}

	private int[] boundIntArr(Property prop, Bound<Integer> bound) {
		int[] prev = prop.getIntList();
		int[] res = new int[prev.length];
		for (int i = 0; i < prev.length; i++) {
			res[i] = bound.clamp(prev[i]);
		}
		prop.set(res);
		return res;
	}

	private double[] boundDoubleArr(Property prop, Bound<Double> bound) {
		double[] prev = prop.getDoubleList();
		double[] res = new double[prev.length];
		for (int i = 0; i < prev.length; i++) {
			res[i] = bound.clamp(prev[i]);
		}
		prop.set(res);
		return res;
	}

	@SuppressWarnings("unchecked")
	private <T extends Number & Comparable<T>> T boundValue(Property prop, Bound<T> bound, T defVal) throws IllegalArgumentException {
		Object b = (Object) bound;
		if (defVal instanceof Integer) {
			return (T) boundInt(prop, (Bound<Integer>) b);
		}
		if (defVal instanceof Double) {
			return (T) boundDouble(prop, (Bound<Double>) b);
		}
		if (defVal instanceof Float) {
			return (T) boundFloat(prop, (Bound<Float>) b);
		}
		throw new IllegalArgumentException(bound.min.getClass().getName() + " is not a valid config type.");
	}

	private Integer boundInt(Property prop, Bound<Integer> bound) {
		prop.set(bound.clamp(prop.getInt()));
		return Integer.valueOf(prop.getInt());
	}

	private Double boundDouble(Property prop, Bound<Double> bound) {
		prop.set(bound.clamp(prop.getDouble()));
		return Double.valueOf(prop.getDouble());
	}

	private Float boundFloat(Property prop, Bound<Float> bound) {
		return boundDouble(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue())).floatValue();
	}

	private final Lang fmlLang = new Lang("fml.configgui.tooltip");

	void addCommentDetails(Property prop, Bound<?> bound) {
		prop.comment += (prop.comment.isEmpty() ? "" : "\n");
		if (bound.equals(Bound.MAX_BOUND)) {
			prop.comment += fmlLang.localize("default", prop.isList() ? Arrays.toString(prop.getDefaults()) : prop.getDefault());
		} else {
			boolean minIsInt = bound.min.doubleValue() == bound.min.intValue();
			boolean maxIsInt = bound.max.doubleValue() == bound.max.intValue();
			prop.comment += fmlLang.localize("defaultNumeric", minIsInt ? bound.min.intValue() : bound.min, maxIsInt ? bound.max.intValue() : bound.max,
					prop.isList() ? Arrays.toString(prop.getDefaults()) : prop.getDefault());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ITypeAdapter getAdapterFor(Field f) {
		TypeToken<?> t = TypeToken.of(f.getGenericType());
		Class<?> c = f.getType();
		for (ITypeAdapter adapter : adapters) {
			if ((c.isPrimitive() && c == adapter.getPrimitiveType()) || adapter.getActualType().isAssignableFrom(t)) {
				return adapter;
			}
		}
		return null;
	}

	public ImmutableSet<String> sections() {
		return ImmutableSet.copyOf(sections);
	}

	public ConfigCategory getCategory(String category) {
		return configFile.getCategory(category);
	}

	public void syncTo(Map<String, Object> values) {
		this.configValues = values;
		for (String s : configValues.keySet()) {
			try {
				Field f = configs.getDeclaredField(s);
				Config annot = f.getAnnotation(Config.class);
				if (annot != null && !getNoSync(f)) {
					Object newVal = configValues.get(s);
					Object oldVal = f.get(null);
					boolean changed = false;
					if (!oldVal.equals(newVal)) {
						CTBMod.logger.debug("Config {}.{} differs from new data. Changing from {} to {}", configs.getName(), f.getName(), oldVal, newVal);
						f.set(null, newVal);
						changed = true;
					}
					if (changed && callback != null) {
						callback.callback(this);
					}
				} else if (annot != null) {
					CTBMod.logger.debug("Skipping syncing field {}.{} as it was marked NoSync", configs.getName(), f.getName());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String[] getComment(Field f) {
		Comment c = f.getAnnotation(Comment.class);
		return c == null ? new String[0] : c.value();
	}

	private Bound<Double> getBound(Field f) {
		Range r = f.getAnnotation(Range.class);
		return r == null ? Bound.MAX_BOUND : Bound.of(r.min(), r.max());
	}

	private boolean getNoSync(Field f) {
		return f.getAnnotation(NoSync.class) != null;
	}

	private RestartReqs getRestartReq(Field f) {
		RestartReq r = f.getAnnotation(RestartReq.class);
		return r == null ? RestartReqs.NONE : r.value();
	}

	/* Event Handling */

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		CTBMod.logger.info("Sending server configs to client for {}", configs.getName());
		PacketHandler.INSTANCE.sendTo(new PacketConfigSync(this), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public void onPlayerLogout(ClientDisconnectionFromServerEvent event) {
		syncTo(originalValues);
		CTBMod.logger.info("Reset configs to client values for {}", configs.getName());
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equals(modid)) {
			process(false);
		}
	}
}
