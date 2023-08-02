package indi.ma;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

//@IFMLLoadingPlugin.Name("ModularMovementsPlugin")
//@IFMLLoadingPlugin.SortingIndex(1001)
public class LoadingPlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass() {
    	return new String[] {"indi.ma.ModularActionsClassTransformer"};
        //return new String[]{"mchhui.modularmovements.coremod.minecraft.EntityPlayerSP", "mchhui.modularmovements.coremod.minecraft.Entity"};
    }

    public String getAccessTransformerClass() {
    	
        return null;
    }

    public String getModContainerClass() {
        return "indi.ma.ModContainer";
    }

    public String getSetupClass() {
        return null;
    }

	@Override
	public void injectData(Map<String, Object> data) {
	}
}