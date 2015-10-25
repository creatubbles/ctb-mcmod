package com.creatubbles.repack.endercore.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import com.creatubbles.repack.enderlib.common.util.BlockCoord;

public abstract class TileEntityBase extends TileEntity {

  private final int checkOffset = (int) (Math.random() * 20);

  @Override
  public final void readFromNBT(NBTTagCompound root) {
    super.readFromNBT(root);
    readCustomNBT(root);
  }

  @Override
  public final void writeToNBT(NBTTagCompound root) {
    super.writeToNBT(root);
    writeCustomNBT(root);
  }

  @Override
  public Packet getDescriptionPacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeCustomNBT(tag);
    return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
  }

  @Override
  public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
    readCustomNBT(pkt.func_148857_g());
  }

  public boolean canPlayerAccess(EntityPlayer player) {
    return !isInvalid() && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
  }

  protected abstract void writeCustomNBT(NBTTagCompound root);

  protected abstract void readCustomNBT(NBTTagCompound root);

  protected void updateBlock() {
    if (worldObj != null) {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
  }

  protected boolean isPoweredRedstone() {
    return worldObj.blockExists(xCoord, yCoord, zCoord) ? worldObj.getStrongestIndirectPower(xCoord, yCoord, zCoord) > 0 : false;
  }

  /**
   * Called directly after the TE is constructed. This is the place to call
   * non-final methods.
   * 
   * Note: This will not be called when the TE is loaded from the save. Hook
   * into the nbt methods for that.
   */
  public void init() {
  }

  private BlockCoord cachedLocation = null;

  public BlockCoord getLocation() {
    return cachedLocation == null || !cachedLocation.equals(xCoord, yCoord, zCoord) ? (cachedLocation = new BlockCoord(this)) : cachedLocation;
  }

  /**
   * Call this with an interval (in ticks) to find out if the current tick is
   * the one you want to do some work. This is staggered so the work of
   * different TEs is stretched out over time.
   * 
   * @see #shouldDoWorkThisTick(int, int) If you need to offset work ticks
   */
  protected boolean shouldDoWorkThisTick(int interval) {
    return shouldDoWorkThisTick(interval, 0);
  }

  /**
   * Call this with an interval (in ticks) to find out if the current tick is
   * the one you want to do some work. This is staggered so the work of
   * different TEs is stretched out over time.
   * 
   * If you have different work items in your TE, use this variant to stagger
   * your work.
   */
  protected boolean shouldDoWorkThisTick(int interval, int offset) {
    return (worldObj.getTotalWorldTime() + checkOffset + offset) % interval == 0;
  }
}
