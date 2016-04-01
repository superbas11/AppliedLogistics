/*
 *
 * LIMITED USE SOFTWARE LICENSE AGREEMENT
 * This Limited Use Software License Agreement (the "Agreement") is a legal agreement between you, the end-user, and the FlatstoneTech Team ("FlatstoneTech"). By downloading or purchasing the software material, which includes source code (the "Source Code"), artwork data, music and software tools (collectively, the "Software"), you are agreeing to be bound by the terms of this Agreement. If you do not agree to the terms of this Agreement, promptly destroy the Software you may have downloaded or copied.
 * FlatstoneTech SOFTWARE LICENSE
 * 1. Grant of License. FlatstoneTech grants to you the right to use the Software. You have no ownership or proprietary rights in or to the Software, or the Trademark. For purposes of this section, "use" means loading the Software into RAM, as well as installation on a hard disk or other storage device. The Software, together with any archive copy thereof, shall be destroyed when no longer used in accordance with this Agreement, or when the right to use the Software is terminated. You agree that the Software will not be shipped, transferred or exported into any country in violation of the U.S. Export Administration Act (or any other law governing such matters) and that you will not utilize, in any other manner, the Software in violation of any applicable law.
 * 2. Permitted Uses. For educational purposes only, you, the end-user, may use portions of the Source Code, such as particular routines, to develop your own software, but may not duplicate the Source Code, except as noted in paragraph 4. The limited right referenced in the preceding sentence is hereinafter referred to as "Educational Use." By so exercising the Educational Use right you shall not obtain any ownership, copyright, proprietary or other interest in or to the Source Code, or any portion of the Source Code. You may dispose of your own software in your sole discretion. With the exception of the Educational Use right, you may not otherwise use the Software, or an portion of the Software, which includes the Source Code, for commercial gain.
 * 3. Prohibited Uses: Under no circumstances shall you, the end-user, be permitted, allowed or authorized to commercially exploit the Software. Neither you nor anyone at your direction shall do any of the following acts with regard to the Software, or any portion thereof:
 * Rent;
 * Sell;
 * Lease;
 * Offer on a pay-per-play basis;
 * Distribute for money or any other consideration; or
 * In any other manner and through any medium whatsoever commercially exploit or use for any commercial purpose.
 * Notwithstanding the foregoing prohibitions, you may commercially exploit the software you develop by exercising the Educational Use right, referenced in paragraph 2. hereinabove.
 * 4. Copyright. The Software and all copyrights related thereto (including all characters and other images generated by the Software or depicted in the Software) are owned by FlatstoneTech and is protected by United States copyright laws and international treaty provisions. FlatstoneTech shall retain exclusive ownership and copyright in and to the Software and all portions of the Software and you shall have no ownership or other proprietary interest in such materials. You must treat the Software like any other copyrighted material. You may not otherwise reproduce, copy or disclose to others, in whole or in any part, the Software. You may not copy the written materials accompanying the Software. You agree to use your best efforts to see that any user of the Software licensed hereunder complies with this Agreement.
 * 5. NO WARRANTIES. FLATSTONETECH DISCLAIMS ALL WARRANTIES, BOTH EXPRESS IMPLIED, INCLUDING BUT NOT LIMITED TO, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE WITH RESPECT TO THE SOFTWARE. THIS LIMITED WARRANTY GIVES YOU SPECIFIC LEGAL RIGHTS. YOU MAY HAVE OTHER RIGHTS WHICH VARY FROM JURISDICTION TO JURISDICTION. FlatstoneTech DOES NOT WARRANT THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED, ERROR FREE OR MEET YOUR SPECIFIC REQUIREMENTS. THE WARRANTY SET FORTH ABOVE IS IN LIEU OF ALL OTHER EXPRESS WARRANTIES WHETHER ORAL OR WRITTEN. THE AGENTS, EMPLOYEES, DISTRIBUTORS, AND DEALERS OF FlatstoneTech ARE NOT AUTHORIZED TO MAKE MODIFICATIONS TO THIS WARRANTY, OR ADDITIONAL WARRANTIES ON BEHALF OF FlatstoneTech.
 * Exclusive Remedies. The Software is being offered to you free of any charge. You agree that you have no remedy against FlatstoneTech, its affiliates, contractors, suppliers, and agents for loss or damage caused by any defect or failure in the Software regardless of the form of action, whether in contract, tort, includinegligence, strict liability or otherwise, with regard to the Software. Copyright and other proprietary matters will be governed by United States laws and international treaties. IN ANY CASE, FlatstoneTech SHALL NOT BE LIABLE FOR LOSS OF DATA, LOSS OF PROFITS, LOST SAVINGS, SPECIAL, INCIDENTAL, CONSEQUENTIAL, INDIRECT OR OTHER SIMILAR DAMAGES ARISING FROM BREACH OF WARRANTY, BREACH OF CONTRACT, NEGLIGENCE, OR OTHER LEGAL THEORY EVEN IF FLATSTONETECH OR ITS AGENT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES, OR FOR ANY CLAIM BY ANY OTHER PARTY. Some jurisdictions do not allow the exclusion or limitation of incidental or consequential damages, so the above limitation or exclusion may not apply to you.
 */

package tech.flatstone.appliedlogistics.common.tileentities.machines;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import tech.flatstone.appliedlogistics.api.features.TechLevel;
import tech.flatstone.appliedlogistics.api.registries.PulverizerRegistry;
import tech.flatstone.appliedlogistics.api.registries.helpers.Crushable;
import tech.flatstone.appliedlogistics.common.blocks.misc.BlockCrank;
import tech.flatstone.appliedlogistics.common.integrations.waila.IWailaBodyMessage;
import tech.flatstone.appliedlogistics.common.items.Items;
import tech.flatstone.appliedlogistics.common.tileentities.TileEntityMachineBase;
import tech.flatstone.appliedlogistics.common.tileentities.inventory.InternalInventory;
import tech.flatstone.appliedlogistics.common.tileentities.inventory.InventoryOperation;
import tech.flatstone.appliedlogistics.common.util.EnumOres;
import tech.flatstone.appliedlogistics.common.util.ICrankable;
import tech.flatstone.appliedlogistics.common.util.InventoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityPulverizer extends TileEntityMachineBase implements ITickable, IWailaBodyMessage, ICrankable {
    private InternalInventory inventory = new InternalInventory(this, 11);
    private float speedMultiplier = 0.0f;
    private float fortuneMultiplier = 0.0f;
    private int maxProcessCount = 1;
    private int ticksRemaining = 0;
    private boolean machineWorking = false;
    private int badCrankCount = 0;
    private int crushIndex = 0;
    private float crushRNG = 0;
    private boolean crushPaused = false;
    private Random rnd = new Random();

    public boolean isCrushPaused() {
        return crushPaused;
    }

    @Override
    public void initMachineData() {
        super.initMachineData();

        NBTTagCompound machineItemData = this.getMachineItemData();
        if (machineItemData != null) {
            for (int i = 0; i < 27; i++) {
                if (machineItemData.hasKey("item_" + i)) {
                    ItemStack item = ItemStack.loadItemStackFromNBT(machineItemData.getCompoundTag("item_" + i));

                    if (ItemStack.areItemsEqual(item, new ItemStack(Items.ITEM_MATERIAL_GEAR.getItem(), 1, EnumOres.WOOD.getMeta())))
                        speedMultiplier = 1.5f * item.stackSize;

                    if (ItemStack.areItemsEqual(item, new ItemStack(Items.ITEM_MATERIAL_GEAR.getItem(), 1, EnumOres.COBBLESTONE.getMeta())))
                        fortuneMultiplier = 0.8f * item.stackSize;
                }
            }
        }

        if (machineItemData == null) {
            // Load Default Details for the machine...
            speedMultiplier = 0.0f;
            fortuneMultiplier = 0.0f;
            maxProcessCount = 1;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);

        speedMultiplier = nbtTagCompound.getFloat("speedMultiplier");
        fortuneMultiplier = nbtTagCompound.getFloat("fortuneMultiplier");
        ticksRemaining = nbtTagCompound.getInteger("ticksRemaining");
        machineWorking = nbtTagCompound.getBoolean("machineWorking");
        maxProcessCount = nbtTagCompound.getInteger("maxProcessCount");
        crushIndex = nbtTagCompound.getInteger("crushIndex");
        crushPaused = nbtTagCompound.getBoolean("crushPaused");
        crushRNG = nbtTagCompound.getFloat("crushRNG");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        nbtTagCompound.setFloat("speedMultiplier", speedMultiplier);
        nbtTagCompound.setFloat("fortuneMultiplier", fortuneMultiplier);
        nbtTagCompound.setInteger("ticksRemaining", ticksRemaining);
        nbtTagCompound.setBoolean("machineWorking", machineWorking);
        nbtTagCompound.setInteger("maxProcessCount", maxProcessCount);
        nbtTagCompound.setInteger("crushIndex", crushIndex);
        nbtTagCompound.setBoolean("crushPaused", crushPaused);
        nbtTagCompound.setFloat("crushRNG", crushRNG);
    }

    @Override
    public IInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InventoryOperation operation, ItemStack removed, ItemStack added) {
        if (slot >= 2 && this.crushPaused) crushPaused = false;
    }

    @Override
    public int[] getAccessibleSlotsBySide(EnumFacing side) {
        return new int[0];
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void doCrank() {
        if (ticksRemaining > 0 && machineWorking) {
            ticksRemaining = ticksRemaining - 20;
            this.markForUpdate();
            this.markDirty();
        }
    }

    @Override
    public boolean canAttachCrank() {
        return true;
    }

    @Override
    public boolean canCrank() {
        if (ticksRemaining > 0 && machineWorking)
            return true;

        badCrankCount++;
        if (badCrankCount > 5) {
            badCrankCount = 0;
            ((BlockCrank) this.worldObj.getBlockState(pos.up()).getBlock()).breakCrank(this.worldObj, this.pos.up(), false);
        }

        return false;
    }

    @Override
    public void update() {
        if (inventory.getStackInSlot(0) != null && inventory.getStackInSlot(1) == null) {
            ItemStack itemIn = inventory.getStackInSlot(0);
            ItemStack itemOut;

            if (!PulverizerRegistry.containsBlock(itemIn))
                return;

            if (itemIn.stackSize - maxProcessCount <= 0) {
                itemOut = itemIn.copy();
                itemIn = null;
            } else {
                itemOut = itemIn.copy();

                itemOut.stackSize = maxProcessCount;
                itemIn.stackSize = itemIn.stackSize - maxProcessCount;
            }

            if (itemIn != null && itemIn.stackSize == 0) itemIn = null;
            if (itemOut.stackSize == 0) itemOut = null;

            inventory.setInventorySlotContents(0, itemIn);
            inventory.setInventorySlotContents(1, itemOut);

            ticksRemaining = 200;   // todo: time registry for pulverizer

            machineWorking = true;

            this.markForUpdate();
            this.markDirty();
        }

        if (ticksRemaining > 0 && machineWorking && getBlockMetadata() == TechLevel.CREATIVE.getMeta())
            ticksRemaining = 0;

        if (ticksRemaining <= 0 && machineWorking) {
            ticksRemaining = 0;
            // Machine Done...

            if (worldObj.isRemote)
                return;

            ItemStack processItem = inventory.getStackInSlot(1);
            if (processItem == null) {
                return;
            }

            ArrayList<Crushable> drops = PulverizerRegistry.getDrops(processItem);

            if (drops.isEmpty()) {
                return;
            }

            if (this.crushPaused) return;

            for (int i = this.crushIndex; i < drops.size(); i++) {
                this.crushIndex = i;
                Crushable crushable = drops.get(this.crushIndex);

                ItemStack outItem = crushable.outItemStack.copy();
                float itemChance = crushable.chance;
                boolean itemFortune = crushable.luckMultiplier == 1.0f;
                if (crushRNG == -1) crushRNG = this.rnd.nextFloat();

                int itemCount = (int) Math.floor((itemChance + crushRNG + outItem.stackSize) * fortuneMultiplier);
                //LogHelper.info(">>> Item Chance: (" + outItem.getUnlocalizedName() + ") " + itemCount);

                outItem.stackSize = itemCount;

                // Simulate placing into output slot...
                if (InventoryHelper.addItemStackToInventory(outItem, inventory, 2, 10, true) != null) {
                    this.crushPaused = true;
                    return;
                }

                InventoryHelper.addItemStackToInventory(outItem, inventory, 2, 10);
                this.crushRNG = -1;
            }

            this.crushIndex = 0;
            inventory.setInventorySlotContents(1, null);

            badCrankCount = 0;
            machineWorking = false;

            this.markForUpdate();
            this.markDirty();


            /**
             * 1. Get a crushable return
             * 2. Generate RNG Item
             * 3. Simulate output
             * 4. if failure -> pause until simuation = true
             * 5. output
             */
        }
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public int getTotalProcessTime() {
        if (ticksRemaining == 0)
            return 0;

        //todo: check itemstack in slot 1 with registry of time
        return 200;
    }

    @Override
    public List<String> getWailaBodyToolTip(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currentTip;
    }

    /**
     * slot 0 = item to pulverize
     * slot 1 = item it is working on (internal)
     * slot 2, 3, 4, 5, 6, 7, 8, 9, 10 = item output
     * slot 11 = fuel input (opt) ???
     */

    /**
     * Stone tier, fuel = side, input = top?
     * all other tiers, input all sides...
     */

    /**
     * Pulverizer Registry:
     *
     * (Item Registry)
     * ItemStack input
     * ItemStack output
     * float chance
     *
     * (Time Registry)
     * ItemStack input
     * int ticks
     *
     * 200 ticks = default * machine multipler
     */
}
