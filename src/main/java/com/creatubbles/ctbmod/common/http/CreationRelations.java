package com.creatubbles.ctbmod.common.http;

import java.util.Map;

import javax.annotation.Nullable;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.request.creation.GetCreationRequest;
import com.creatubbles.api.response.creation.GetCreationResponse;
import com.creatubbles.api.response.relationships.Relationships;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.util.ConcurrentUtil;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.google.common.util.concurrent.ListenableFuture;

import jersey.repackaged.com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;

@Getter
public class CreationRelations extends Creation {
	
	@Delegate
	private final Creation creation;
	
	@Nullable
	@Setter
	private Relationships relationships;
	
	@Getter
	@Nullable
	private transient ListenableFuture<?> completionCallback;
	
	public CreationRelations(@NonNull Creation c, @Nullable Relationships r) {
		creation = c;
		relationships = r;
	}
	
	public CreationRelations(GetCreationResponse resp) {
	    this(resp.getCreation(), resp.getRelationships());
	}
	
    private static final Map<String, CreationRelations> completing = Maps.newConcurrentMap();

    /**
     * Adds relationships to a creation, used for completing old NBT data.
     * 
     * The returned object will have a null relationship object until the async task completes.
     * 
     * @param c
     *            The creation to complete
     * @param tag
     *            The tag to write the updated data to
     * @return A soon-to-be compledted {@link CreationRelations} object.
     */
    public static CreationRelations complete(Creation c, final NBTTagCompound tag) {
        if (completing.containsKey(c.getId())) {
            return completing.get(c.getId());
        }
        final GetCreationRequest req = new GetCreationRequest(c.getId(), CTBMod.cache.getOAuth().getAccessToken());
        final CreationRelations ret = new CreationRelations(c, null);
        ret.completionCallback = ConcurrentUtil.execute(new Runnable() { { completing.put(ret.getCreation().getId(), ret); }

            @Override
            public void run() {
                GetCreationResponse resp = req.execute().getResponse();
                ret.setRelationships(resp.getRelationships());
                NBTUtil.writeJsonToNBT(ret, tag);
                completing.remove(ret.getCreation().getId());
            }
        });
        return ret;
    }
}
