package indi.ma.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import indi.ma.common.PlayerState;
import indi.ma.network.TacticalHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
public class ServerListener {
    public static Map playerNotStepMap;
    public static Map playerStateMap;
    public static Method setSize;

    static {
        ServerListener.playerStateMap = new HashMap();
        ServerListener.playerNotStepMap = new HashMap();
    }

    public static double getCameraProbeOffset(Integer id) {
        return ServerListener.playerStateMap.containsKey(id) ? ((double)((PlayerState)ServerListener.playerStateMap.get(id)).probeOffset) : 0.0;
    }

    public static boolean isCrawling(Integer id) {
        return ServerListener.playerStateMap.containsKey(id) ? ((PlayerState)ServerListener.playerStateMap.get(id)).isCrawling : false;
    }

    public static boolean isSitting(Integer id) {
        return ServerListener.playerStateMap.containsKey(id) ? ((PlayerState)ServerListener.playerStateMap.get(id)).isSitting : false;
    }

    public void onFMLInit(FMLInitializationEvent event) {
        ServerListener.setSize = ReflectionHelper.findMethod(Entity.class, null,new String[] {"setSize", "func_70105_a"}, new Class[]{Float.TYPE, Float.TYPE});
    }

    public static Vec3 onGetPositionEyes(EntityPlayer player, float partialTicks, Vec3 vec3d) {
    	Vec3 vecRotY=Vec3.createVectorHelper(ServerListener.getCameraProbeOffset(Integer.valueOf(player.getEntityId())) * -0.6, 0.0, 0.0);
    	vecRotY.rotateAroundY(((float)(((double)(-Minecraft.getMinecraft().thePlayer.rotationYaw)) * 3.141593 / 180.0)));
        return ServerListener.getCameraProbeOffset(Integer.valueOf(player.getEntityId())) == 0.0 ? vec3d : 
        	vec3d.addVector(vecRotY.xCoord,vecRotY.yCoord,vecRotY.zCoord);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerListener.playerStateMap.put(Integer.valueOf(event.player.getEntityId()), new PlayerState());
        TacticalHandler.sendClientConfig(((EntityPlayerMP)event.player));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerListener.playerStateMap.remove(Integer.valueOf(event.player.getEntityId()));
        ServerListener.playerNotStepMap.remove(Integer.valueOf(event.player.getEntityId()));
    }

    @SubscribeEvent
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
        if(((event.entity instanceof EntityPlayer)) && (ServerListener.playerNotStepMap.containsKey(Integer.valueOf(event.entity.getEntityId()))) 
        		&& ((long)(((Long)ServerListener.playerNotStepMap.get(Integer.valueOf(event.entity.getEntityId()))))) > System.currentTimeMillis() 
        		&& (event.name.contains("step"))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            ServerListener.updateOffset(Integer.valueOf(event.player.getEntityId()));
            if(ServerListener.isSitting(Integer.valueOf(event.player.getEntityId()))) {
                if(event.player.eyeHeight != 1.1f) {
                    event.player.eyeHeight = 1.1f;
                }
            }
            else if(ServerListener.isCrawling(Integer.valueOf(event.player.getEntityId()))) {
                if(event.player.eyeHeight != 0.4f) {
                    event.player.eyeHeight = 0.4f;
                }
            }
            else if(event.player.eyeHeight == 0.4f) {
                EntityPlayer v4 = event.player;
                v4.eyeHeight = event.player.getDefaultEyeHeight();
            }
            else if(event.player.eyeHeight == 1.1f) {
                EntityPlayer v4_1 = event.player;
                v4_1.eyeHeight = event.player.getDefaultEyeHeight();
            }
            float f = event.player.width;
            float f1 = event.player.height;
            if(ServerListener.isSitting(Integer.valueOf(event.player.getEntityId()))) {
                f1 = 1.2f;
                event.player.setSneaking(false);
                //event.player.yOffset=1.1f;
            }
            else if(ServerListener.isCrawling(Integer.valueOf(event.player.getEntityId()))) {
                f1 = 0.5f;
                event.player.setSneaking(false);
                //event.player.yOffset=0.4f;
            }else {
            	f1=1.8F;
            	//event.player.yOffset=0.0F;
            }
           double offset= ServerListener.getCameraProbeOffset(event.player.getEntityId());
           
            if(f != event.player.width || f1 != event.player.height) {
                AxisAlignedBB axisalignedbb = event.player.boundingBox;//getEntityBoundingBox()
                axisalignedbb = AxisAlignedBB.getBoundingBox(
                		axisalignedbb.minX,
                		axisalignedbb.minY,
                		axisalignedbb.minZ,
                		axisalignedbb.minX + ((double)f),
                		axisalignedbb.minY + ((double)f1),
                		axisalignedbb.minZ + ((double)f));
                if(event.player.worldObj.func_147461_a(axisalignedbb).isEmpty()) {
                    try {
                    	//event.player.yOffset=1.1f;
                        ServerListener.setSize.invoke(event.player, ((float)f), ((float)f1));
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            if (offset != 0.0F&&false)
			{
            	Vec3 vec3d = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D)
            			.addVector(offset * -0.5D, 0.0D, 0.0D);
				vec3d.rotateAroundY(event.player.rotationYaw * 3.14F / 180.0F);
				AxisAlignedBB bb=event.player.boundingBox;
				bb.minX=bb.minX + vec3d.xCoord;
				bb.minZ=bb.minZ + vec3d.zCoord;
				bb.maxX=bb.maxX + vec3d.xCoord;
				bb.maxZ=bb.maxZ + vec3d.zCoord;
				try {
					ServerListener.setSize.invoke(event.player,new Object[] { Float.valueOf(f),Float.valueOf(f1) });
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }
    }

    public static void updateOffset(Integer id) {
        if(!ServerListener.playerStateMap.containsKey(id)) {
            return;
        }
        ((PlayerState)ServerListener.playerStateMap.get(id)).updateOffset();
    }
}


