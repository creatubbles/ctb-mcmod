package com.creatubbles.ctbmod.common.creator;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.ArrayUtils;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.network.MessageDimensionChange;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.repack.endercore.api.common.util.IProgressTile;
import com.creatubbles.repack.endercore.common.TileEntityBase;
import com.creatubbles.repack.endercore.common.util.Bound;

public class TileCreator extends TileEntityBase implements ISidedInventory, IUpdatePlayerListBox, IProgressTile {

	private static final Bound<Integer> DIMENSION_BOUND = Bound.of(1, 16);
	
	private ItemStack[] inventory = new ItemStack[5];
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

    public void create(Creation creation) {
        this.creating = creation;
        for (int i = 0; i < 4; i++) {
            inventory[i].stackSize -= i == 0 ? getRequiredPaper() : getRequiredDye();
            if (inventory[i].stackSize == 0) {
                inventory[i] = null;
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
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == 4;
    }
    
    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return index < 4;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
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
        if (isItemValidForSlot(index, stack)) {
            this.inventory[index] = stack;
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

	private String[] colors = new String[]{ "dyeRed", "dyeGreen", "dyeBlue"};
	
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack == null) {
            return true;
        }
        if (index == 0) {
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
