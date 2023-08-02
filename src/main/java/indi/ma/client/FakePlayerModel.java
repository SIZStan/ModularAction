package indi.ma.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class FakePlayerModel extends ModelBiped {
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		if(true)
		{
			return;
		}
        //ClientLitener.setRotationAngles(this, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		ModelBiped model=this;
		if (entityIn.isSneaking()) {
			model.bipedRightLeg.rotateAngleX = -1.413717f;
	        model.bipedRightLeg.rotateAngleY = 0.314159f;
	        model.bipedRightLeg.rotateAngleZ = 0.07854f;
	        model.bipedLeftLeg.rotateAngleX = -1.413717f;
	        model.bipedLeftLeg.rotateAngleY = -0.314159f;
	        model.bipedLeftLeg.rotateAngleZ = -0.07854f;	        
		}
		//isCrawling
		if(false)
		{
			model.bipedHead.rotateAngleX = (float)(((double)model.bipedHead.rotateAngleX) - 1.221111);
		      model.bipedRightArm.rotateAngleX = (float)(((double)model.bipedRightArm.rotateAngleX) * 0.2);
		      model.bipedLeftArm.rotateAngleX = (float)(((double)model.bipedLeftArm.rotateAngleX) * 0.2);
		      model.bipedRightArm.rotateAngleX = (float)(((double)model.bipedRightArm.rotateAngleX) + 3.14);
		      model.bipedLeftArm.rotateAngleX = (float)(((double)model.bipedLeftArm.rotateAngleX) + 3.14);
		      model.bipedRightLeg.rotateAngleX = (float)(((double)model.bipedRightLeg.rotateAngleX) * 0.2);
		      model.bipedLeftLeg.rotateAngleX = (float)(((double)model.bipedLeftLeg.rotateAngleX) * 0.2);
		}
		
		//probe
		if(true)
		{
			float v3=0f;
			if(v3 >= 0.0f) {
                model.bipedRightLeg.rotateAngleZ = (float)(((double)model.bipedRightLeg.rotateAngleZ) + ((double)(20.0f * v3)) * 3.14 / 180.0);
                model.bipedRightLeg.rotateAngleZ=0.2f;
                return;
            }
            model.bipedLeftLeg.rotateAngleZ = (float)(((double)model.bipedLeftLeg.rotateAngleZ) + ((double)(20.0f * v3)) * 3.14 / 180.0);
            
		}
		
		
		float v3;
        //PlayerState v4;
        if(((entityIn instanceof EntityPlayer)) && (entityIn.isEntityAlive())) {
//            if(entityIn == Minecraft.getMinecraft().player) {
//                v4 = ClientLitener.clientPlayerState;
//                v3 = ClientLitener.cameraProbeOffset;
//            }
//            else {
//                if(!ClientLitener.ohterPlayerStateMap.containsKey(Integer.valueOf(entityIn.getEntityId()))) {
//                    return;
//                }
//                v4 = (PlayerState)ClientLitener.ohterPlayerStateMap.get(Integer.valueOf(entityIn.getEntityId()));
//                v3 = v4.probeOffset;
//            }
//            if(v4.isSitting) {
//                model.bipedRightLeg.rotateAngleX = -1.413717f;
//                model.bipedRightLeg.rotateAngleY = 0.314159f;
//                model.bipedRightLeg.rotateAngleZ = 0.07854f;
//                model.bipedLeftLeg.rotateAngleX = -1.413717f;
//                model.bipedLeftLeg.rotateAngleY = -0.314159f;
//                model.bipedLeftLeg.rotateAngleZ = -0.07854f;
//            }
//            if(v4.isCrawling) {
//                model.bipedHead.rotateAngleX = (float)(((double)model.bipedHead.rotateAngleX) - 1.221111);
//                model.bipedRightArm.rotateAngleX = (float)(((double)model.bipedRightArm.rotateAngleX) * 0.2);
//                model.bipedLeftArm.rotateAngleX = (float)(((double)model.bipedLeftArm.rotateAngleX) * 0.2);
//                model.bipedRightArm.rotateAngleX = (float)(((double)model.bipedRightArm.rotateAngleX) + 3.14);
//                model.bipedLeftArm.rotateAngleX = (float)(((double)model.bipedLeftArm.rotateAngleX) + 3.14);
//                if((entityIn instanceof AbstractClientPlayer)) {
//                    ItemStack itemstack = ((AbstractClientPlayer)entityIn).getHeldItemMainhand();
//                    if(itemstack != ItemStack.EMPTY && !itemstack.isEmpty() && (ModularMovements.mwfEnable) && ((itemstack.getItem() instanceof BaseItem))) {
//                        model.bipedLeftArm.rotateAngleY = 0.0f;
//                        model.bipedRightArm.rotateAngleY = 0.0f;
//                        model.bipedLeftArm.rotateAngleX = 3.14f;
//                        model.bipedRightArm.rotateAngleX = 3.14f;
//                    }
//                }
//                model.bipedRightLeg.rotateAngleX = (float)(((double)model.bipedRightLeg.rotateAngleX) * 0.2);
//                model.bipedLeftLeg.rotateAngleX = (float)(((double)model.bipedLeftLeg.rotateAngleX) * 0.2);
//            }
//            if(v3 >= 0.0f) {
//                model.bipedRightLeg.rotateAngleZ = (float)(((double)model.bipedRightLeg.rotateAngleZ) + ((double)(20.0f * v3)) * 3.14 / 180.0);
//                return;
//            }
            //model.bipedLeftLeg.rotateAngleZ = (float)(((double)model.bipedLeftLeg.rotateAngleZ) + ((double)(20.0f * 1)) * 3.14 / 180.0);
            return;
        }
		
//        copyModelAngles(model.bipedLeftLeg, model.bipedLeftLegwear);
//        copyModelAngles(model.bipedRightLeg, model.bipedRightLegwear);
//        copyModelAngles(model.bipedLeftArm, model.bipedLeftArmwear);
//        copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
//        copyModelAngles(model.bipedBody, model.bipedBodyWear);
//        copyModelAngles(model.bipedHead, model.bipedHeadwear);
    }
	 public static void copyModelAngles(ModelRenderer source, ModelRenderer dest)
    {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
    }
}
