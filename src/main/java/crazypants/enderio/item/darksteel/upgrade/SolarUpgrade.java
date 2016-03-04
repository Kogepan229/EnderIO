package crazypants.enderio.item.darksteel.upgrade;

import com.enderio.core.client.render.RenderUtil;

import crazypants.enderio.EnderIO;
import crazypants.enderio.config.Config;
import crazypants.enderio.item.darksteel.DarkSteelItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SolarUpgrade extends AbstractUpgrade {

  private static final String KEY_LEVEL = "level";
  
  private static final String UPGRADE_NAME = "speedBoost";
  
  public static final SolarUpgrade SOLAR_ONE = new SolarUpgrade("enderio.darksteel.upgrade.solar_one", (byte) 1, Config.darkSteelSolarOneCost);
  public static final SolarUpgrade SOLAR_TWO = new SolarUpgrade("enderio.darksteel.upgrade.solar_two", (byte) 2, Config.darkSteelSolarTwoCost);
  
  private Render render;

  public static SolarUpgrade loadFromItem(ItemStack stack) {
    if(stack == null) {
      return null;
    }
    if(stack.getTagCompound() == null) {
      return null;
    }
    if(!stack.getTagCompound().hasKey(KEY_UPGRADE_PREFIX + UPGRADE_NAME)) {
      return null;
    }
    return new SolarUpgrade((NBTTagCompound) stack.getTagCompound().getTag(KEY_UPGRADE_PREFIX + UPGRADE_NAME));
  }

  private static ItemStack createUpgradeItem() {
    ItemStack pot = new ItemStack(Items.potionitem, 1, 0);
    int res = PotionHelper.applyIngredient(0, Items.nether_wart.getPotionEffect(new ItemStack(Items.nether_wart)));
    res = PotionHelper.applyIngredient(res, PotionHelper.sugarEffect);    
    pot.setItemDamage(res);
    return pot;
  }
  
  private byte level;

  public SolarUpgrade(NBTTagCompound tag) {
    super(UPGRADE_NAME, tag);
    level = tag.getByte(KEY_LEVEL);
  }

  public SolarUpgrade(String unlocName, byte level, int levelCost) {
    super(UPGRADE_NAME, unlocName, createUpgradeItem(), levelCost);
    this.level = level;
  }
  
  @Override
  public boolean canAddToItem(ItemStack stack) {
      if(stack == null || stack.getItem() != DarkSteelItems.itemDarkSteelHelmet || !EnergyUpgrade.itemHasAnyPowerUpgrade(stack)) {
        return false;
      }
      SolarUpgrade up = loadFromItem(stack);
      if(up == null) {
        return level == 1;
      }
      return up.level == level - 1;
  }
  
  @Override
  public boolean hasUpgrade(ItemStack stack) {
    if(!super.hasUpgrade(stack)) {
      return false;
    }
    SolarUpgrade up = loadFromItem(stack);
    if(up == null) {
      return false;
    }
    return up.unlocName.equals(unlocName);
  }
  
  @Override
  public ItemStack getUpgradeItem() {
    return new ItemStack(EnderIO.blockSolarPanel, 1, level - 1);
  }

  @Override
  public void writeUpgradeToNBT(NBTTagCompound upgradeRoot) {
    upgradeRoot.setByte(KEY_LEVEL, level);
  }

  public int getRFPerSec() {
    return level == 1 ? Config.darkSteelSolarOneGen : Config.darkSteelSolarTwoGen;
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public IRenderUpgrade getRender() {
    return render == null ? render = new Render() : render;
  }

  @SideOnly(Side.CLIENT)
  private class Render implements IRenderUpgrade {

    private EntityItem item = new EntityItem(Minecraft.getMinecraft().theWorld);
    private ItemStack panel1 = new ItemStack(EnderIO.blockSolarPanel, 1, 0);
    private ItemStack panel2 = new ItemStack(EnderIO.blockSolarPanel, 1, 1);

    @Override
    public void render(RenderPlayerEvent event, ItemStack stack, boolean head) {
      if (head) {
        //TODO: 1.8
        RenderUtil.bindBlockTexture();
        GlStateManager.depthMask(true);        
        item.hoverStart = 0;
        Helper.translateToHeadLevel(event.entityPlayer);
        GlStateManager.translate(0, -0.155, 0);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.scale(2.1f, 2.1f, 2.1f);
        byte level = loadFromItem(stack).level;
        item.setEntityItemStack(level == 0 ? panel1 : panel2);        
        Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(item, 0, 0, 0, 0, 0);
        
        GlStateManager.depthMask(false);
      }
    }
  }
}
