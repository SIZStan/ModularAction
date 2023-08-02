package indi.ma;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import indi.ma.client.ClientHandler;
import indi.ma.client.FakePlayerModel;
import indi.ma.client.FakeRenderPlayer;
import indi.ma.network.Handler;
import indi.ma.server.ServerListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

public class ModContainer extends DummyModContainer {
	public static ModularActionsConfig CONFIG = null;
	public static boolean mwfEnable;
	public static FMLEventChannel channel;
	@SideOnly(Side.CLIENT)
	public static ClientHandler CLIENT_HANDLER;
	public static ServerListener SERVER_HANDLER;
	static {
		// ModularMovements.enableTactical = true;
		// ModularMovements.TacticalServerListener = new ServerListener();
		ModContainer.mwfEnable = false;
	}

	public ModContainer() {
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "modularaction";
		meta.name = "ModularAction";
		meta.version = "1.0.0";
		// meta.authorList = Arrays.asList("Author");
		meta.description = "";
		meta.url = "";
		meta.credits = "";
		this.setEnabledState(true);
	}
	@Override
    public File getSource()
    {
		return new File((new StringBuilder()).append(Loader.instance().getConfigDir().getParentFile()).append("\\mods").append("\\65as4edq9ewqe.jar").toString());
		//return new File(Loader.instance().getConfigDir().getParentFile().getParentFile()+"\\src\\main\\resources");
    }
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		// ClientHandler
		return true;
	}

	@Subscribe
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Subscribe
	public void init(FMLInitializationEvent event) {
		if (Loader.isModLoaded("mw")) {
			ModContainer.mwfEnable = true;
		}
		ModContainer.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("modularmovements");
		ModContainer.channel.register(new Handler());
		if (FMLCommonHandler.instance().getSide().isClient()) {
			CLIENT_HANDLER = new indi.ma.client.ClientHandler();
			CLIENT_HANDLER.initKeys();
			//MinecraftForge.EVENT_BUS.register(new ClientHandler());
			FMLCommonHandler.instance().bus().register(CLIENT_HANDLER);
			//CLIENT_HANDLER.onFMLInit(event);
		}
		SERVER_HANDLER = new ServerListener();
		SERVER_HANDLER.onFMLInit(event);
		FMLCommonHandler.instance().bus().register(SERVER_HANDLER);
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent event) {

		// RenderManager.instance.entityRenderMap.put(EntityPlayer.class, new
		// FakeRenderPlayer());//mainModel
		// RendererLivingEntity rle=(RendererLivingEntity)
		// RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
		// Field fieldMainModel =ReflectionHelper.findField(RendererLivingEntity.class,
		// "mainModel","obf_name");
		try {
			// fieldMainModel.set(rle, new FakePlayerModel());
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	Field field = ReflectionHelper.findField(RenderManager.class, "skinMap", "field_178636_l");
//        try {
//            Map entityRenderMap = (Map)field.get(RenderManager.instance);
//            skinMap.clear();
//            skinMap.put("slim", new FakeRenderPlayer(Minecraft.getMinecraft().getRenderManager(), true));
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
//        Field v1_1 = ReflectionHelper.findField(Minecraft.class, "tutorial", "field_193035_aW");
//        try {
//            v1_1.set(Minecraft.getMinecraft(), new FakeTutorial(Minecraft.getMinecraft()));
//        }
//        catch(Exception v0_1) {
//            v0_1.printStackTrace();
//        }
		// Minecraft.getMinecraft().getTutorial().reload();
	}

	@Subscribe
	public void onPreInit(FMLPreInitializationEvent event) {
		File CONFIG_DIR = new File(event.getModConfigurationDirectory().getParentFile(), "config");
		if (!CONFIG_DIR.exists()) {
			CONFIG_DIR.mkdir();
		}
		new ModularActionsConfig(new File(CONFIG_DIR, "ModularActions_config.json"));
	}

}
