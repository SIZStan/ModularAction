package indi.ma.client;

import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import com.vicmatskiv.weaponlib.compatibility.Interceptors;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
@SideOnly(Side.CLIENT)
public class FakeRenderPlayer extends RenderPlayer {
	public FakeRenderPlayer() {
		super();
        setRenderManager(RenderManager.instance);
        //super(new ModelBiped(0.0F), 0.5F);
        this.mainModel = new FakePlayerModel();
//        int i;
//        for(i = 0; i < this.layerRenderers.size(); ++i) {
//            if(((LayerRenderer)this.layerRenderers.get(i)).getClass() == LayerBipedArmor.class) {
//                this.layerRenderers.remove(i);
//                break;
//            }
//        }
//        if(ModularMovements.mwfEnable) {
//            this.addLayer(new FakeLayerBipedArmor(((RenderLivingBase)this)));
//            this.addLayer(new RenderLayerBackpack(this, this.getMainModel().bipedBodyWear));
//            this.addLayer(new RenderLayerBody(this, this.getMainModel().bipedBodyWear));
//            this.addLayer(new RenderLayerHeldGun(((RenderLivingBase)this)));
//        }
    }

	@Override
	protected void rotateCorpse(EntityLivingBase entityLiving, float p_77043_2_, float rotationYaw, float partialTicks)
    {
		
		if(false)
		{
			//example
			//GL11.glTranslatef(0.0F, entityLiving.height + 0.1F, 0.0F);
			//GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
			//example
			//isCrawling
			//GL11.glRotatef(180.0f - rotationYaw, 0.0f, 1.0f, 0.0f);
            //GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
            //GL11.glTranslated(0.0, -1.3, 0.1);
            //GL11.glTranslated(0 * 0.4, 0.0, 0.0);
            
			float probeOffset=0.5f;
            if(probeOffset != 0.0f) {
                GL11.glRotatef(180.0f - entityLiving.rotationYawHead, 0.0f, 1.0f, 0.0f);
                GL11.glTranslated(((double)probeOffset) * 0.1, 0.0, 0.0);
                GL11.glRotatef(180.0f - entityLiving.rotationYawHead, 0.0f, -1.0f, 0.0f);
                GL11.glRotatef(180.0f - rotationYaw, 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(probeOffset * -20.0f, 0.0f, 0.0f, 1.0f);
            }
		}else {
			super.rotateCorpse(entityLiving, p_77043_2_, rotationYaw, partialTicks);
		}
    }
//    protected void applyRotations(AbstractClientPlayer entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
//        if(ClientLitener.applyRotations(((RenderLivingBase)this), ((EntityLivingBase)entityLiving), p_77043_2_, rotationYaw, partialTicks)) {
//            return;
//        }
//        super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
//    }

//    protected void applyRotations(EntityLivingBase arg1, float arg2, float arg3, float arg4) {
//        this.applyRotations(((AbstractClientPlayer)arg1), arg2, arg3, arg4);
//    }
	
//	@Override
//	protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_)
//    {
//        this.bindEntityTexture(p_77036_1_);
//
//        if (!p_77036_1_.isInvisible())
//        {
//        	//replaced by mw//maybe same time???
//        	this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
//        	Interceptors.render2(this.mainModel, p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
//        }
//        else if (!p_77036_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer))
//        {
//            GL11.glPushMatrix();
//            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
//            GL11.glDepthMask(false);
//            GL11.glEnable(GL11.GL_BLEND);
//            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
//            this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
//            GL11.glDisable(GL11.GL_BLEND);
//            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
//            GL11.glPopMatrix();
//            GL11.glDepthMask(true);
//        }
//        else
//        {
//            this.mainModel.setRotationAngles(p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_, p_77036_1_);
//        }
//    }
}
