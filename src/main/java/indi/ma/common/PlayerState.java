package indi.ma.common;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import indi.ma.ModContainer;
import indi.ma.client.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

public class PlayerState {
    @SideOnly(Side.CLIENT)
    public static class ClientPlayerState extends PlayerState {
        //private static Field equippedProgressMainHandField;

        public ClientPlayerState() {
        }

        @Override
        public void enableCrawling() {
            super.enableCrawling();
            ClientHandler.crawlingMousePosXMove = 0;
        }

        @Override
        public void leftProbe() {
            super.leftProbe();
//            if(Minecraft.getMinecraft().player.getPrimaryHand() != EnumHandSide.LEFT) {
//                Minecraft.getMinecraft().player.setPrimaryHand(EnumHandSide.LEFT);
//                this.updateEquippedItem();
//            }
        }

        @Override  // mchhui.modularmovements.tactical.PlayerState
        public void resetProbe() {
            super.resetProbe();
//            if(Minecraft.getMinecraft().player.getPrimaryHand() != Minecraft.getMinecraft().gameSettings.mainHand) {
//                Minecraft.getMinecraft().player.setPrimaryHand(Minecraft.getMinecraft().gameSettings.mainHand);
//                this.updateEquippedItem();
//            }
        }

        @Override  // mchhui.modularmovements.tactical.PlayerState
        public void rightProbe() {
            super.rightProbe();
//            if(Minecraft.getMinecraft().player.getPrimaryHand() != EnumHandSide.RIGHT) {
//                Minecraft.getMinecraft().player.setPrimaryHand(EnumHandSide.RIGHT);
//                this.updateEquippedItem();
//            }
        }

        private void updateEquippedItem() {
            try {
            	//refresh equipedProgress
                //ClientPlayerState.equippedProgressMainHandField.set(Minecraft.getMinecraft().getItemRenderer(), Float.valueOf(-0.4f));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isCrawling;
    public boolean isSitting;
    private long lastCrawl;
    private long lastCharge;
    private long lastProbe;
    private long lastSit;
    //private long lastSlide;
    private long lastSyncTime;
    public byte probe;
    public float probeOffset;

    public PlayerState() {
        this.isSitting = false;
        this.isCrawling = false;
        this.probe = 0;
        this.probeOffset = 0.0f;
        this.lastSyncTime = 0L;
    }

    public boolean canCharging() {
    	return System.currentTimeMillis() - this.lastCharge > TimeUnit.SECONDS.toMillis(((long)ModContainer.CONFIG.cooldown.chargeCooldown));
    }
//    public boolean canSlide() {
//    	return System.currentTimeMillis() - this.lastSlide > TimeUnit.SECONDS.toMillis(((long)ModContainer.CONFIG.cooldown.slideCooldown));
//    }
    public boolean canCrawl() {
        return System.currentTimeMillis() - this.lastCrawl > TimeUnit.SECONDS.toMillis(((long)ModContainer.CONFIG.cooldown.crawlCooldown));
    }
    public boolean canProbe() {
    	//
        return !this.isCrawling&&System.currentTimeMillis() - this.lastProbe > TimeUnit.SECONDS.toMillis(((long)ModContainer.CONFIG.cooldown.leanCooldown));
    }
    public boolean canSit() {
        return System.currentTimeMillis() - this.lastSit > TimeUnit.SECONDS.toMillis(((long)ModContainer.CONFIG.cooldown.sitCooldown));
    }

    public void disableCrawling() {
        this.isCrawling = false;
    }

    public void disableSit() {
        this.isSitting = false;
    }

    public void enableCrawling() {
        this.isCrawling = true;
        this.disableSit();
        if(canCharging()&&(Minecraft.getMinecraft()).thePlayer.isSprinting())
        {
        	this.lastCharge = System.currentTimeMillis();
        }
        this.lastCrawl = System.currentTimeMillis();
    }

    public void enableSit() {
        this.isSitting = true;
        this.disableCrawling();
        if(canCharging()&&Minecraft.getMinecraft().thePlayer.isSprinting())
        {
        	this.lastCharge = System.currentTimeMillis();
        }else {
        	this.lastSit = System.currentTimeMillis();
        }
        
    }

    public boolean isStanding() {
        return !this.isCrawling && !this.isSitting;
    }

    public void leftProbe() {
        this.probe = -1;
        this.lastProbe = System.currentTimeMillis();
    }

    public void readCode(int arg4) {
        boolean v1 = true;
        this.probe = (byte)(arg4 % 10 - 1);
        int v4 = arg4 / 10;
        this.isCrawling = v4 % 10 != 0;
        if(v4 / 10 % 10 == 0) {
            v1 = false;
        }
        this.isSitting = v1;
    }

    public void reset() {
        this.readCode(1);
    }

    public void resetProbe() {
        this.probe = 0;
    }

    public void rightProbe() {
        this.probe = 1;
        this.lastProbe = System.currentTimeMillis();
    }

    public void updateOffset() {
        double amplifer = ((double)(System.currentTimeMillis() - this.lastSyncTime)) * 0.06;
        this.lastSyncTime = System.currentTimeMillis();
        //System.out.println("updateOffset: "+this.probeOffset);
        if(this.probe == -1) {
            if(this.probeOffset > -1.0f) {
                this.probeOffset = (float)(((double)this.probeOffset) - 0.1 * amplifer);
            }
            if(this.probeOffset < -1.0f) {
                this.probeOffset = -1.0f;
            }
        }
        if(this.probe == 1) {
            if(this.probeOffset < 1.0f) {
                this.probeOffset = (float)(((double)this.probeOffset) + 0.1 * amplifer);
            }
            if(this.probeOffset > 1.0f) {
                this.probeOffset = 1.0f;
            }
        }
        if(this.probe == 0) {
            if(((double)Math.abs(this.probeOffset)) <= 0.1 * amplifer) {
                this.probeOffset = 0.0f;
            }
            if(this.probeOffset < 0.0f) {
                this.probeOffset = (float)(((double)this.probeOffset) + 0.1 * amplifer);
            }
            if(this.probeOffset > 0.0f) {
                this.probeOffset = (float)(((double)this.probeOffset) - 0.1 * amplifer);
            }
        }
    }

    public int writeCode() {
        int v0 = (this.isSitting ? 1 : 0) * 100;
        return this.isCrawling ? v0 + 10 + this.probe + 1 : v0 + this.probe + 1;
    }
}	
