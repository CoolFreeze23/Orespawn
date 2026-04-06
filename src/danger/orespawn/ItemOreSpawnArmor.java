/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemArmor
 *  net.minecraft.item.ItemArmor$ArmorMaterial
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.ArmorStats;
import danger.orespawn.OreSpawnMain;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemOreSpawnArmor
extends ItemArmor {
    private int armor_material = 0;
    private int armor_type = 0;
    private int original_d = 0;

    public ItemOreSpawnArmor(int par1, ItemArmor.ArmorMaterial par2EnumArmorMaterial, int par3, int par4) {
        super(par2EnumArmorMaterial, par3, par4);
        this.func_77637_a(CreativeTabs.field_78037_j);
        this.armor_material = 0;
        if (par2EnumArmorMaterial == OreSpawnMain.armorLAVAEEL) {
            this.armor_material = 1;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorMOTHSCALE) {
            this.armor_material = 2;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorEMERALD) {
            this.armor_material = 3;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorEXPERIENCE) {
            this.armor_material = 4;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorRUBY) {
            this.armor_material = 5;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorAMETHYST) {
            this.armor_material = 6;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorPINK) {
            this.armor_material = 7;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorTIGERSEYE) {
            this.armor_material = 8;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorPEACOCK) {
            this.armor_material = 9;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorMOBZILLA) {
            this.armor_material = 10;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorROYAL) {
            this.armor_material = 11;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorLAPIS) {
            this.armor_material = 12;
        }
        if (par2EnumArmorMaterial == OreSpawnMain.armorQUEEN) {
            this.armor_material = 13;
        }
        this.armor_type = par4;
        this.original_d = this.field_77879_b;
    }

    public int get_armor_material() {
        return this.armor_material;
    }

    public int get_armor_type() {
        return this.armor_type;
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        ArmorStats a = null;
        if (this.armor_material == 0) {
            a = OreSpawnMain.Ultimate_armorstats;
        }
        if (this.armor_material == 1) {
            a = OreSpawnMain.LavaEel_armorstats;
        }
        if (this.armor_material == 2) {
            a = OreSpawnMain.MothScale_armorstats;
        }
        if (this.armor_material == 3) {
            a = OreSpawnMain.Emerald_armorstats;
        }
        if (this.armor_material == 4) {
            a = OreSpawnMain.Experience_armorstats;
        }
        if (this.armor_material == 5) {
            a = OreSpawnMain.Ruby_armorstats;
        }
        if (this.armor_material == 6) {
            a = OreSpawnMain.Amethyst_armorstats;
        }
        if (this.armor_material == 7) {
            a = OreSpawnMain.Pink_armorstats;
        }
        if (this.armor_material == 8) {
            a = OreSpawnMain.TigersEye_armorstats;
        }
        if (this.armor_material == 9) {
            a = OreSpawnMain.Peacock_armorstats;
        }
        if (this.armor_material == 10) {
            a = OreSpawnMain.Mobzilla_armorstats;
        }
        if (this.armor_material == 11) {
            a = OreSpawnMain.Royal_armorstats;
        }
        if (this.armor_material == 12) {
            a = OreSpawnMain.Lapis_armorstats;
        }
        if (this.armor_material == 13) {
            a = OreSpawnMain.Queen_armorstats;
        }
        if (a != null) {
            if (a.e_protection != 0) {
                par1ItemStack.func_77966_a(Enchantment.field_77332_c, a.e_protection);
            }
            if (a.e_fireprotection != 0) {
                par1ItemStack.func_77966_a(Enchantment.field_77329_d, a.e_fireprotection);
            }
            if (a.e_blastprotection != 0) {
                par1ItemStack.func_77966_a(Enchantment.field_77327_f, a.e_blastprotection);
            }
            if (a.e_projectileprotection != 0) {
                par1ItemStack.func_77966_a(Enchantment.field_77328_g, a.e_projectileprotection);
            }
            if (a.e_unbreaking != 0) {
                par1ItemStack.func_77966_a(Enchantment.field_77347_r, a.e_unbreaking);
            }
            if (this.armor_type == 3 && a.e_featherfalling != 0) {
                par1ItemStack.func_77966_a(Enchantment.field_77330_e, a.e_featherfalling);
            }
            if (this.armor_type == 0) {
                if (a.e_respiration != 0) {
                    par1ItemStack.func_77966_a(Enchantment.field_77340_h, a.e_respiration);
                }
                if (a.e_aquaaffinity != 0) {
                    par1ItemStack.func_77966_a(Enchantment.field_77341_i, a.e_aquaaffinity);
                }
            }
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        ArmorStats a = null;
        int lvl = 0;
        int enchanted = 0;
        if (this.armor_material == 0) {
            a = OreSpawnMain.Ultimate_armorstats;
        }
        if (this.armor_material == 1) {
            a = OreSpawnMain.LavaEel_armorstats;
        }
        if (this.armor_material == 2) {
            a = OreSpawnMain.MothScale_armorstats;
        }
        if (this.armor_material == 3) {
            a = OreSpawnMain.Emerald_armorstats;
        }
        if (this.armor_material == 4) {
            a = OreSpawnMain.Experience_armorstats;
        }
        if (this.armor_material == 5) {
            a = OreSpawnMain.Ruby_armorstats;
        }
        if (this.armor_material == 6) {
            a = OreSpawnMain.Amethyst_armorstats;
        }
        if (this.armor_material == 7) {
            a = OreSpawnMain.Pink_armorstats;
        }
        if (this.armor_material == 8) {
            a = OreSpawnMain.TigersEye_armorstats;
        }
        if (this.armor_material == 9) {
            a = OreSpawnMain.Peacock_armorstats;
        }
        if (this.armor_material == 10) {
            a = OreSpawnMain.Mobzilla_armorstats;
        }
        if (this.armor_material == 11) {
            a = OreSpawnMain.Royal_armorstats;
        }
        if (this.armor_material == 12) {
            a = OreSpawnMain.Lapis_armorstats;
        }
        if (this.armor_material == 13) {
            a = OreSpawnMain.Queen_armorstats;
        }
        if (a != null) {
            enchanted = a.e_aquaaffinity + a.e_blastprotection + a.e_featherfalling + a.e_fireprotection;
            if ((enchanted += a.e_projectileprotection + a.e_protection + a.e_respiration + a.e_unbreaking) > 0) {
                lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77332_c.field_77352_x, (ItemStack)stack);
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77329_d.field_77352_x, (ItemStack)stack);
                }
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77327_f.field_77352_x, (ItemStack)stack);
                }
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77328_g.field_77352_x, (ItemStack)stack);
                }
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77340_h.field_77352_x, (ItemStack)stack);
                }
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77341_i.field_77352_x, (ItemStack)stack);
                }
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77347_r.field_77352_x, (ItemStack)stack);
                }
                if (lvl <= 0) {
                    lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77330_e.field_77352_x, (ItemStack)stack);
                }
                if (lvl == 0) {
                    if (a.e_protection != 0) {
                        stack.func_77966_a(Enchantment.field_77332_c, a.e_protection);
                    }
                    if (a.e_fireprotection != 0) {
                        stack.func_77966_a(Enchantment.field_77329_d, a.e_fireprotection);
                    }
                    if (a.e_blastprotection != 0) {
                        stack.func_77966_a(Enchantment.field_77327_f, a.e_blastprotection);
                    }
                    if (a.e_projectileprotection != 0) {
                        stack.func_77966_a(Enchantment.field_77328_g, a.e_projectileprotection);
                    }
                    if (a.e_unbreaking != 0) {
                        stack.func_77966_a(Enchantment.field_77347_r, a.e_unbreaking);
                    }
                    if (this.armor_type == 3 && a.e_featherfalling != 0) {
                        stack.func_77966_a(Enchantment.field_77330_e, a.e_featherfalling);
                    }
                    if (this.armor_type == 0) {
                        if (a.e_respiration != 0) {
                            stack.func_77966_a(Enchantment.field_77340_h, a.e_respiration);
                        }
                        if (a.e_aquaaffinity != 0) {
                            stack.func_77966_a(Enchantment.field_77341_i, a.e_aquaaffinity);
                        }
                    }
                }
            }
        }
    }

    public String getArmorTexture(ItemStack armor, Entity e, int slot, String layer) {
        if (this.armor_type == 0 || this.armor_type == 1 || this.armor_type == 3) {
            if (this.armor_material == 0) {
                return "orespawn:ultimate_1.png";
            }
            if (this.armor_material == 1) {
                return "orespawn:lavaeel_1.png";
            }
            if (this.armor_material == 2) {
                return "orespawn:mothscale_1.png";
            }
            if (this.armor_material == 4) {
                return "orespawn:experience_1.png";
            }
            if (this.armor_material == 5) {
                return "orespawn:ruby_1.png";
            }
            if (this.armor_material == 6) {
                return "orespawn:amethyst_1.png";
            }
            if (this.armor_material == 7) {
                return "orespawn:pink_1.png";
            }
            if (this.armor_material == 8) {
                return "orespawn:tigerseye_1.png";
            }
            if (this.armor_material == 9) {
                return "orespawn:peacock_1.png";
            }
            if (this.armor_material == 10) {
                return "orespawn:mobzilla_1.png";
            }
            if (this.armor_material == 11) {
                return "orespawn:royal_1.png";
            }
            if (this.armor_material == 12) {
                return "orespawn:lapis_1.png";
            }
            if (this.armor_material == 13) {
                return "orespawn:queen_1.png";
            }
            return "orespawn:emerald_1.png";
        }
        if (this.armor_material == 0) {
            return "orespawn:ultimate_2.png";
        }
        if (this.armor_material == 1) {
            return "orespawn:lavaeel_2.png";
        }
        if (this.armor_material == 2) {
            return "orespawn:mothscale_2.png";
        }
        if (this.armor_material == 4) {
            return "orespawn:experience_2.png";
        }
        if (this.armor_material == 5) {
            return "orespawn:ruby_2.png";
        }
        if (this.armor_material == 6) {
            return "orespawn:amethyst_2.png";
        }
        if (this.armor_material == 7) {
            return "orespawn:pink_2.png";
        }
        if (this.armor_material == 8) {
            return "orespawn:tigerseye_2.png";
        }
        if (this.armor_material == 9) {
            return "orespawn:peacock_2.png";
        }
        if (this.armor_material == 10) {
            return "orespawn:mobzilla_2.png";
        }
        if (this.armor_material == 11) {
            return "orespawn:royal_2.png";
        }
        if (this.armor_material == 12) {
            return "orespawn:lapis_2.png";
        }
        if (this.armor_material == 13) {
            return "orespawn:queen_2.png";
        }
        return "orespawn:emerald_2.png";
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        ItemStack boots = null;
        Object ia = null;
        Object it = null;
        if ((this.armor_material == 11 || this.armor_material == 9) && player != null && (boots = player.func_71124_b(1)) != null && (boots.func_77973_b() == OreSpawnMain.RoyalBoots && OreSpawnMain.RoyalGlideEnable != 0 || boots.func_77973_b() == OreSpawnMain.PeacockFeatherBoots)) {
            if (player.field_70181_x < (double)-0.1f) {
                player.field_70181_x = -0.1f;
            }
            player.field_70143_R = 0.0f;
        }
        if (this.armor_material == 13 && player != null && (boots = player.func_71124_b(1)) != null && boots.func_77973_b() == OreSpawnMain.QueenBoots && OreSpawnMain.RoyalGlideEnable != 0) {
            if (player.field_70181_x < -0.25) {
                player.field_70181_x = -0.25;
            }
            player.field_70143_R = 0.0f;
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

