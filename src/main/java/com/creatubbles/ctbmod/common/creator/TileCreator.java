package com.creatubbles.ctbmod.common.creator;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import org.apache.commons.lang3.ArrayUtils;

import com.creatubbles.ctbmod.common.network.MessageDimensionChange;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.repack.endercore.common.TileEntityBase;
import com.creatubbles.repack.endercore.common.util.Bound;

public class TileCreator extends TileEntityBase implements IInventory {

	private static final Bound<Integer> DIMENSION_BOUND = Bound.of(1, 16);

	private ItemStack[] inventory = new ItemStack[5];

	@Getter
	private int width, height;

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

    public void setOutput(ItemStack created) {
        setInventorySlotContents(4, created);
    }

	public ItemStack[] getInput() {
		return ArrayUtils.subarray(inventory, 0, inventory.length - 1);
    }

    public boolean canCreate() {
        if (inventory[0] != null && inventory[0].stackSize >= getRequiredPaper()) {
            for (int i = 1; i < 4; i++) {
                if (inventory[i] == null || inventory[i].stackSize < getRequiredDye()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int getRequiredPaper() {
        return (3 + getWidth() * getHeight()) / 4;
    }

    public int getRequiredDye() {
        return (7 + getWidth() * getHeight()) / 8;
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
		if (this.inventory[index] != null) {
			ItemStack itemstack;
			if (this.inventory[index].stackSize <= count) {
				itemstack = this.inventory[index];
				this.inventory[index] = null;
				return itemstack;
			} else {
				itemstack = this.inventory[index].splitStack(count);
				if (this.inventory[index].stackSize == 0) {
					this.inventory[index] = null;
				}
				return itemstack;
			}
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		if (this.inventory[index] != null) {
			ItemStack itemstack = this.inventory[index];
			this.inventory[index] = null;
			return itemstack;
		} else {
			return null;
		}
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory[index] = stack;
        markDirty();
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
	public void clear() {
		for (int i = 0; i < inventory.length; i++) {
			inventory[i] = null;
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return false;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound root) {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.inventory.length; ++i) {
			if (this.inventory[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(nbttagcompound1);
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

			this.inventory[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}
		
		setWidth(root.getInteger("paintingWidth"));
		setHeight(root.getInteger("paintingHeight"));
	}

	// Stupid pointless IInventory methods

	@Override
	public String getCommandSenderName() {
		return getDisplayName().getUnformattedText();
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentText("This is stupid");
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}
}
