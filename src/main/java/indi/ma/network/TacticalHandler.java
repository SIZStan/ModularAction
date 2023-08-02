package indi.ma.network;

import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import indi.ma.ModContainer;
import indi.ma.client.ClientHandler;
import indi.ma.common.PlayerState;
import indi.ma.server.ServerListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

public class TacticalHandler {
    public static enum EnumPacketType {
        STATE,
        SET_STATE,
        NOFALL,
        NOSTEP,
        MOD_CONFIG;

    }

    private static PlayerState state;

    public static void onHandle(FMLNetworkEvent.ClientCustomPacketEvent event) {
        ByteBuf buffer = (ByteBuf)event.packet.payload();
        switch(((EnumPacketType)Handler.readEnumValue(EnumPacketType.class,buffer))) {
            case STATE: {
                int id = buffer.readInt();
                int code = buffer.readInt();
                if(id != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                    if(!ClientHandler.ohterPlayerStateMap.containsKey(Integer.valueOf(id))) {
                    	ClientHandler.ohterPlayerStateMap.put(Integer.valueOf(id), new PlayerState());
                    }
                    TacticalHandler.state = (PlayerState)ClientHandler.ohterPlayerStateMap.get(Integer.valueOf(id));
                    TacticalHandler.state.readCode(code);
                    return;
                }
                return;
            }
            case SET_STATE: {
                int client_code = buffer.readInt();
                ClientHandler.clientPlayerState.readCode(client_code);
                return;
            }
            case MOD_CONFIG: {
                boolean withGunsOnly = buffer.readBoolean();
                float slideMaxForce = buffer.readFloat();
                boolean blockView = buffer.readBoolean();
                float blockAngle = buffer.readFloat();
                float sitCooldown = buffer.readFloat();
                float crawlCooldown = buffer.readFloat();
                ModContainer.CONFIG.lean.withGunsOnly = withGunsOnly;
                ModContainer.CONFIG.slide.maxForce = slideMaxForce;
                ModContainer.CONFIG.crawl.blockView = blockView;
                ModContainer.CONFIG.crawl.blockAngle = blockAngle;
                ModContainer.CONFIG.cooldown.sitCooldown = sitCooldown;
                ModContainer.CONFIG.cooldown.crawlCooldown = crawlCooldown;
                return;
            }
            default: {
                return;
            }
        }
    }

    public static void onHandle(FMLNetworkEvent.ServerCustomPacketEvent event) {
    	ByteBuf buffer = event.packet.payload();
        EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
        switch(((EnumPacketType)Handler.readEnumValue(EnumPacketType.class,buffer))) {
            case STATE: {
                int code = buffer.readInt();
                if(ServerListener.playerStateMap.containsKey(Integer.valueOf(((EntityPlayer)player).getEntityId()))) {
                    TacticalHandler.state = (PlayerState)ServerListener.playerStateMap.get(Integer.valueOf(((EntityPlayer)player).getEntityId()));
                    if(code == TacticalHandler.state.writeCode()) {
                        return;
                    }
                    TacticalHandler.state.readCode(code);
                    TacticalHandler.sendToClient(((EntityPlayer)player), code);
                    return;
                }
                return;
            }
            case NOFALL: {
                ((EntityPlayer)player).fallDistance = 0.0f;
                return;
            }
            case NOSTEP: {
                int time = buffer.readInt();
                ServerListener.playerNotStepMap.put(Integer.valueOf(((EntityPlayer)player).getEntityId()), Long.valueOf(System.currentTimeMillis() + ((long)time)));
                return;
            }
            default: {
                return;
            }
        }
    }

    public static void sendClientConfig(EntityPlayerMP entityPlayer) {
    	ByteBuf buffer = Unpooled.buffer();
        Handler.writeEnumValue(EnumFeatures.Tactical,buffer);
        Handler.writeEnumValue(EnumPacketType.MOD_CONFIG,buffer);
        buffer.writeBoolean(ModContainer.CONFIG.lean.withGunsOnly);
        buffer.writeFloat(ModContainer.CONFIG.slide.maxForce);
        buffer.writeBoolean(ModContainer.CONFIG.crawl.blockView);
        buffer.writeFloat(ModContainer.CONFIG.crawl.blockAngle);
        buffer.writeFloat(ModContainer.CONFIG.cooldown.sitCooldown);
        buffer.writeFloat(ModContainer.CONFIG.cooldown.crawlCooldown);
        ModContainer.channel.sendTo(new FMLProxyPacket(buffer, "modularmovements"), entityPlayer);
    }

    public static void sendNoFall() {
        ByteBuf buffer = Unpooled.buffer();
        Handler.writeEnumValue(EnumFeatures.Tactical,buffer);
        Handler.writeEnumValue(EnumPacketType.NOFALL,buffer);
        ModContainer.channel.sendToServer(new FMLProxyPacket(buffer, "modularmovements"));
    }

    public static void sendNoStep(int time) {
        ByteBuf buffer = Unpooled.buffer();
        Handler.writeEnumValue(EnumFeatures.Tactical,buffer);
        Handler.writeEnumValue(EnumPacketType.NOSTEP,buffer);
        buffer.writeInt(time);
        ModContainer.channel.sendToServer(new FMLProxyPacket(buffer, "modularmovements"));
    }

    public static void sendStateSettng(EntityPlayerMP entityPlayer, int code) {
        ByteBuf buffer = Unpooled.buffer();
        Handler.writeEnumValue(EnumFeatures.Tactical,buffer);
        Handler.writeEnumValue(EnumPacketType.SET_STATE,buffer);
        buffer.writeInt(code);
        ModContainer.channel.sendTo(new FMLProxyPacket(buffer, "modularmovements"), entityPlayer);
    }

    public static void sendToClient(EntityPlayer entityPlayer, int code) {
        ByteBuf buffer = Unpooled.buffer();
        Handler.writeEnumValue(EnumFeatures.Tactical,buffer);
        Handler.writeEnumValue(EnumPacketType.STATE,buffer);
        buffer.writeInt(entityPlayer.getEntityId());
        buffer.writeInt(code);
        ModContainer.channel.sendToAll(new FMLProxyPacket(buffer, "modularmovements"));
    }

    public static void sendToServer(int code) {
        ByteBuf buffer = Unpooled.buffer();
        Handler.writeEnumValue(EnumFeatures.Tactical,buffer);
        Handler.writeEnumValue(EnumPacketType.STATE,buffer);
        buffer.writeInt(code);
        ModContainer.channel.sendToServer(new FMLProxyPacket(buffer, "modularmovements"));
    }
}

