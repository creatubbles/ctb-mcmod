package com.creatubbles.ctbmod.common.creator;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.ArrayUtils;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.network.MessageDimensionChange;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.repack.endercore.api.common.util.IProgressTile;
import com.creatubbles.repack.endercore.common.TileEntityBase;
import com.creatubbles.repack.endercore.common.util.Bound;

public class TileCreator extends TileEntityBase implements ISidedInventory, IProgressTile {

    private static final Bound<Integer> DIMENSION_BOUND = Bound.of(1, 16);

    private final ItemStack[] inventory = new ItemStack[5];
    private Creation creating;
    private int progress;

    @Getter
    private int width = 1, height = 1;

    public void setWidth(int width) {
        this.width = DIMENSION_BOUND.clamp(width);
        dimensionsChanged();
    }

    public void setHeight(int height) {
        this.height = DIMENSION_BOUND.clamp(height);
        dimensionsChanged();
    }

    public int getMinSize() {
        return DIMENSION_BOUND.getMin();
    }

    public int getMaxSize() {
        return DIMENSION_BOUND.getMax();
    }

    private void dimensionsChanged() {
        if (worldObj != null && worldObj.isRemote) {
            PacketHandler.INSTANCE.sendToServer(new MessageDimensionChange(this));
        }
    }

    public ItemStack getOutput() {
        return inventory[4];
    }

    public int getPaperCount() {
        int count = inventory[0] == null ? 0 : inventory[0].stackSize;
        if (!Configs.harderPaintings) {
            for (int i = 1; i < 4; i++) {
                count += inventory[i] == null ? 0 : inventory[i].stackSize;
            }
        }
        return count;
    }

    public int getLowestDyeCount() {
        int ret = 0;
        for (int i = 1; i < 4; i++) {
            if (inventory[i] != null) {
                ret = Math.max(ret, inventory[i].stackSize);
            }
        }
        return ret;
    }

    public void create(Creation creation) {
        if (!canCreate()) {
            return;
        }
        creating = creation;
        if (Configs.harderPaintings) {
            for (int i = 0; i < 4; i++) {
                inventory[i].stackSize -= i == 0 ? getRequiredPaper() : getRequiredDye();
                if (inventory[i].stackSize == 0) {
                    inventory[i] = null;
                }
            }
        } else {
            int required = getRequiredPaper();
            for (int i = 0; i < 4 && required > 0; i++) {
                if (inventory[i] != null) {
                    if (inventory[i].stackSize < required) {
                        required -= inventory[i].stackSize;
                        inventory[i] = null;
                    } else {
                        inventory[i].stackSize -= required;
                        required = 0;
                        if (inventory[i].stackSize == 0) {
                            inventory[i] = null;
                        }
                    }
                }
            }
        }
    }

    public ItemStack[] getInput() {
        return ArrayUtils.subarray(inventory, 0, inventory.length - 1);
    }

    public boolean canCreate() {
        if (inventory[4] != null || progress > 0) {
            return false;
        }
        return getPaperCount() >= getRequiredPaper() && (!Configs.harderPaintings || getLowestDyeCount() >= getRequiredDye());
    }

    public int getRequiredPaper() {
        return (3 + getWidth() * getHeight()) / 4;
    }

    public int getRequiredDye() {
        return (7 + getWidth() * getHeight()) / 8;
    }

    @Override
    protected void doUpdate() {
        if (!worldObj.isRemote) {
            if (creating == null) {
                progress = 0;
            } else {
                if (progress < 20) {
                    progress++;
                } else {
                    inventory[4] = BlockPainting.create(creating, width, height);
                    markDirty();
                    creating = null;
                }
            }
        }
    }

    @Override
    public float getProgress() {
        return progress / 20f;
    }

    @Override
    public void setProgress(float progress) {
        this.progress = progress < 0 ? 0 : (int) (progress * 20);
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        return index == 4;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        return index < 4;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int index) {
        return new int[] { 0, 1, 2, 3, 4 };
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory[index % inventory.length];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inventory[index] != null) {
            ItemStack itemstack;
            if (inventory[index].stackSize <= count) {
                itemstack = inventory[index];
                inventory[index] = null;
                return itemstack;
            } else {
                itemstack = inventory[index].splitStack(count);
                if (inventory[index].stackSize == 0) {
                    inventory[index] = null;
                }
                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (inventory[index] != null) {
            ItemStack itemstack = inventory[index];
            inventory[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (isItemValidForSlot(index, stack)) {
            inventory[index] = stack;
            markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public String getInventoryName() {
        return "tile.ctbmod.creator";
    }

    private final String[] colors = new String[] { "dyeRed", "dyeGreen", "dyeBlue" };

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack == null) {
            return true;
        }
        if (index == 0 || !Configs.harderPaintings && index < 4) {
            return stack.getItem() == Items.paper;
        } else if (index < 4) {
            int[] ids = OreDictionary.getOreIDs(stack);
            String ore = colors[index - 1];
            for (int i : ids) {
                if (OreDictionary.getOreName(i).equals(ore)) {
                    return true;
                }
            }
        } else {
            return Block.getBlockFromItem(stack.getItem()) == CTBMod.painting;
        }
        return false;
    }

    @Override
    protected void writeCustomNBT(NBTTagCompound root) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < inventory.length; ++i) {
            if (inventory[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        root.setTag("Items", nbttaglist);

        root.setInteger("paintingWidth", getWidth());
        root.setInteger("paintingHeight", getHeight());
    }

    @Override
    protected void readCustomNBT(NBTTagCompound root) {
        NBTTagList nbttaglist = root.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            inventory[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
        }

        setWidth(root.getInteger("paintingWidth"));
        setHeight(root.getInteger("paintingHeight"));
    }
}
