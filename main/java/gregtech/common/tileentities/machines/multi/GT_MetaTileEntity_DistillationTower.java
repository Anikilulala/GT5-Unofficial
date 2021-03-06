package gregtech.common.tileentities.machines.multi;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Recipe.GT_Recipe_Map;
import gregtech.api.util.GT_Utility;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class GT_MetaTileEntity_DistillationTower
  extends GT_MetaTileEntity_MultiBlockBase
{
  public GT_MetaTileEntity_DistillationTower(int aID, String aName, String aNameRegional)
  {
    super(aID, aName, aNameRegional);
  }
  
  public GT_MetaTileEntity_DistillationTower(String aName)
  {
    super(aName);
  }
  
  public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity)
  {
    return new GT_MetaTileEntity_DistillationTower(this.mName);
  }
  
  public String[] getDescription()
  {
    return new String[] { "Controller Block for the Distillation Tower", "Size: 3x6x3 (Hollow)", "Controller (front bottom)", "1x Input Hatch (bottom)", "5x Output Hatch (one each height level besides botton)","1x Output Bus (Botton)", "1x Energy Hatch (anywhere)", "1x Maintenance Hatch (anywhere)", "Clean Stainless Steel Casings for the rest (26 at least!)" };
  }
  
  public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone)
  {
    if (aSide == aFacing) {
      return new ITexture[] { Textures.BlockIcons.CASING_BLOCKS[49], new GT_RenderedTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER_ACTIVE : Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER) };
    }
    return new ITexture[] { Textures.BlockIcons.CASING_BLOCKS[49] };
  }
  
  public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity)
  {
    return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "VacuumFreezer.png");
  }
  
  public GT_Recipe.GT_Recipe_Map getRecipeMap()
  {
    return GT_Recipe.GT_Recipe_Map.sDistillationRecipes;
  }
  
  public boolean isCorrectMachinePart(ItemStack aStack)
  {
    return true;
  }
  
  public boolean isFacingValid(byte aFacing)
  {
    return aFacing > 1;
  }
  
  public boolean checkRecipe(ItemStack aStack)
  {
    
      long tVoltage = getMaxInputVoltage();
      byte tTier = (byte)Math.max(1, GT_Utility.getTier(tVoltage));
      if(this.mInputHatches.size()>0&&this.mInputHatches.get(0)!=null&&this.mInputHatches.get(0).mFluid!=null&&this.mInputHatches.get(0).mFluid.amount>0){
      GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sDistillationRecipes.findRecipe(getBaseMetaTileEntity(), false, gregtech.api.enums.GT_Values.V[tTier], new FluidStack[]{this.mInputHatches.get(0).mFluid}, new ItemStack[] {});
      if (tRecipe != null) {
        if (tRecipe.isRecipeInputEqual(true, new FluidStack[]{this.mInputHatches.get(0).mFluid}, new ItemStack[] {}))
        {
          this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
          this.mEfficiencyIncrease = 10000;
          if (tRecipe.mEUt <= 16)
          {
            this.mEUt = (tRecipe.mEUt * (1 << tTier - 1) * (1 << tTier - 1));
            this.mMaxProgresstime = (tRecipe.mDuration / (1 << tTier - 1));
          }
          else
          {
            this.mEUt = tRecipe.mEUt;
            this.mMaxProgresstime = tRecipe.mDuration;
            while (this.mEUt <= gregtech.api.enums.GT_Values.V[(tTier - 1)])
            {
              this.mEUt *= 4;
              this.mMaxProgresstime /= 2;
            }
          }
          if (this.mEUt > 0) {
            this.mEUt = (-this.mEUt);
          }
          this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);
          this.mOutputItems = new ItemStack[] { tRecipe.getOutput(0) };
						this.mOutputFluids = tRecipe.mFluidOutputs;
          updateSlots();
          return true;
        }
      }}
    
    return false;
  }
  private static boolean controller;
  public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack)
  {
    int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX;int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ;
    if (!aBaseMetaTileEntity.getAirOffset(xDir, 1, zDir)) {
      return false;
    }
    int tAmount = 0;
				  controller=false;
    for (int i = -1; i < 2; i++) {
      for (int j = -1; j < 2; j++) {
        for (int h = 0; h < 6; h++) {
          if (!(i==0&&j==0&&(h>0&&h<5)))//((h > 0)&&(h<5)) || (((xDir + i != 0) || (zDir + j != 0)) && ((i != 0) || (j != 0)))
          {
            IGregTechTileEntity tTileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + i, h, zDir + j);
            if ((!addMaintenanceToMachineList(tTileEntity, 49)) && (!addInputToMachineList(tTileEntity, 49)) && (!addOutputToMachineList(tTileEntity, 49)) && (!addEnergyInputToMachineList(tTileEntity, 49))&&(!ignoreController(aBaseMetaTileEntity.getBlockOffset(xDir + i, h, zDir + j))))
            {
              if (aBaseMetaTileEntity.getBlockOffset(xDir + i, h, zDir + j) != GregTech_API.sBlockCasings4) {
                  return false;
              }
								if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, h, zDir + j) != 1) {
                	return false;
              }
              tAmount++;
            }
          }
        }
      }
    }
					if(this.mInputHatches.size()!=1||this.mOutputBusses.size()!=1||this.mInputBusses.size()!=0||this.mOutputHatches.size()!=5){return false;}
					int height = this.getBaseMetaTileEntity().getYCoord();
					if(this.mInputHatches.get(0).getBaseMetaTileEntity().getYCoord()!=height||this.mOutputBusses.get(0).getBaseMetaTileEntity().getYCoord()!=height){return false;}
					GT_MetaTileEntity_Hatch_Output[] tmpHatches = new GT_MetaTileEntity_Hatch_Output[5];
					for(int i=0;i< this.mOutputHatches.size();i++){
						int hatchNumber = this.mOutputHatches.get(i).getBaseMetaTileEntity().getYCoord()-1-height;
						if(tmpHatches[hatchNumber]==null){
							tmpHatches[hatchNumber]=this.mOutputHatches.get(i);
						}else{return false;}
					}
    return tAmount >= 26;
  }

				public boolean ignoreController(Block tTileEntity){
					if(!controller&&tTileEntity == GregTech_API.sBlockMachines){return true;}
					return false;
				}
  
  public int getMaxEfficiency(ItemStack aStack)
  {
    return 10000;
  }
  
  public int getPollutionPerTick(ItemStack aStack)
  {
    return 0;
  }
  
  public int getDamageToComponent(ItemStack aStack)
  {
    return 0;
  }
  
  public int getAmountOfOutputs()
  {
    return 1;
  }
  
  public boolean explodesOnComponentBreak(ItemStack aStack)
  {
    return false;
  }
}