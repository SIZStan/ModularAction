package indi.ma.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import indi.ma.common.PlayerState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;

public class Handler {
	@SubscribeEvent
    public void onHandle(FMLNetworkEvent.ClientCustomPacketEvent event) {
        switch(((EnumFeatures)readEnumValue(EnumFeatures.class,(event.packet.payload())))) {
            case Tactical: {
                TacticalHandler.onHandle(event);
                return;
            }
            default: {
                return;
            }
        }
    }

    @SubscribeEvent
    public void onHandle(FMLNetworkEvent.ServerCustomPacketEvent event) {
        switch(((EnumFeatures)readEnumValue(EnumFeatures.class,(event.packet.payload())))) {
            case Tactical: {
                TacticalHandler.onHandle(event);
                return;
            }
            default: {
                return;
            }
        }
    }
    public static <T extends Enum<T>> T readEnumValue(Class<T> enumClass,ByteBuf bb)
    {
        return (T)((Enum[])enumClass.getEnumConstants())[readVarIntFromBuffer(bb)];
    }
    public static int readVarIntFromBuffer(ByteBuf bb)
    {
        int i = 0;
        int j = 0;
        byte b0;

        do
        {
            b0 = bb.readByte();
            i |= (b0 & 127) << j++ * 7;

            if (j > 5)
            {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((b0 & 128) == 128);

        return i;
    }

    public static ByteBuf writeEnumValue(Enum<?> value,ByteBuf bb)
    {
    	writeVarIntToBuffer(value.ordinal(),bb);
        return bb;
    }
    public static void writeVarIntToBuffer(int p_150787_1_,ByteBuf bb)
    {
        while ((p_150787_1_ & -128) != 0)
        {
        	bb.writeByte(p_150787_1_ & 127 | 128);
            p_150787_1_ >>>= 7;
        }

        bb.writeByte(p_150787_1_);
    }
}
