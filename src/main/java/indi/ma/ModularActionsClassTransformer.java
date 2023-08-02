package indi.ma;

import cpw.mods.fml.common.FMLLog;
import indi.ma.client.ClientHandler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.Vec3;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ModularActionsClassTransformer implements IClassTransformer{

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(transformedName.equals("net.minecraft.client.renderer.entity.RendererLivingEntity")) {
			return transformRendererLivingEntity(basicClass);
		}
		if(transformedName.equals("net.minecraft.client.renderer.EntityRenderer")) {
			return transformEntityRenderer(basicClass);
		}
		if(transformedName.equals("net.minecraft.client.entity.EntityPlayerSP")) {
			basicClass= transformEntityPlayerSP2(!name.equals(transformedName),basicClass);
			return transformPlayerSP(basicClass);
		}
		if(transformedName.equals("net.minecraft.client.entity.EntityPlayerSP")) {
			
		}
		return basicClass;
	}
	public byte[] transformEntityPlayerSP2(boolean isObf,byte[] basicClass){
		FMLLog.warning("[Transforming:net.minecraft.client.renderer.entity.EntityPlayerSP2]");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "getPosition", "(F)Lnet/minecraft/util/Vec3;", null, null);
//			{
//			av0 = mv.visitAnnotation("Lcpw/mods/fml/relauncher/SideOnly;", true);
//			av0.visitEnum("value", "Lcpw/mods/fml/relauncher/Side;", "CLIENT");
//			av0.visitEnd();
//			}
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(683, l0);
		
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(FLOAD, 1);
//			mv.visitMethodInsn(INVOKESTATIC, "indi/ma/client/ClientHandler", "onGetPositionEyes", "(Lnet/minecraft/entity/EntityLivingBase;F)Lnet/minecraft/util/Vec3;", false);
//			mv.visitInsn(ARETURN);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLocalVariable("this", "Lnet/minecraft/client/entity/EntityPlayerSP;", null, l0, l1, 0);
//			mv.visitLocalVariable("p_70666_1_", "F", null, l0, l1, 1);
		
//			mv.visitMaxs(2, 2);
//			mv.visitEnd();
//		}
		MethodNode methodNode;
		//boolean isObf=true;
		if(isObf)
		{
			methodNode = new MethodNode(0x50000, Opcodes.ACC_PUBLIC, "func_70666_h", "(F)Lnet/minecraft/util/Vec3;", null, null);
		}else {
			methodNode = new MethodNode(0x50000, Opcodes.ACC_PUBLIC, "getPosition", "(F)Lnet/minecraft/util/Vec3;", null, null);
		}
		methodNode.visitMaxs(2, 2);
		InsnList ilist=new InsnList();
		LabelNode startLabelNode = new LabelNode();
        LabelNode endLabelNode = new LabelNode();
		ilist.add(startLabelNode);
		ilist.add(new VarInsnNode(Opcodes.ALOAD,0));
		ilist.add(new VarInsnNode(Opcodes.FLOAD,1));
		ilist.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "indi/ma/client/ClientHandler",
				"onGetPositionEyes", "(Lnet/minecraft/entity/player/EntityPlayer;F)Lnet/minecraft/util/Vec3;", false));
		ilist.add(new InsnNode(Opcodes.ARETURN));
		ilist.add(endLabelNode);
		methodNode.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/entity/EntityPlayerSP;", null, startLabelNode, endLabelNode, 0));
        methodNode.localVariables.add(new LocalVariableNode("partialTicks", "F", null, startLabelNode, endLabelNode, 1));
        methodNode.instructions.add(ilist);
        classNode.methods.add(methodNode);
        
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
        FMLLog.warning("[Transformed:net.minecraft.client.renderer.entity.EntityPlayerSP2]");
        return classWriter.toByteArray();
	}
	public byte[] transformPlayerSP(byte[] basicClass){
		FMLLog.warning("[Transforming:net.minecraft.client.renderer.entity.EntityPlayerSP]");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		for(MethodNode methodNode : classNode.methods)
		{
			//System.out.println("P1");
			//System.out.println(methodNode.name);
			//match?
			boolean isObf=true;
			if(methodNode.name.equals("onLivingUpdate"))
			{
				isObf=false;
			}
			if(methodNode.name.equals("onLivingUpdate")||methodNode.name.equals("e")) 
			{// (Lnet/minecraft/entity/EntityLivingBase;FFF)V //(Lsv;FFF)V
				//System.out.println("P2");
				if(methodNode.desc.equals("()V"))
				{
					//System.out.println("P3");
					//System.out.println("a (Lsv;FFF)V matched!");
					AbstractInsnNode insertPoint;
					for(AbstractInsnNode ainNode : methodNode.instructions.toArray())
					{
//						mv.visitVarInsn(ALOAD, 0);
//						mv.visitFieldInsn(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "movementInput", "Lnet/minecraft/util/MovementInput;");
//						mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/MovementInput", "updatePlayerMoveState", "()V", false);
//						Label l43 = new Label();
//						mv.visitLabel(l43);
//						mv.visitLineNumber(191, l43);
						
//						mv.visitVarInsn(ALOAD, 0);
//						mv.visitFieldInsn(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "movementInput", "Lnet/minecraft/util/MovementInput;");
//						mv.visitMethodInsn(INVOKESTATIC, "indi/ma/client/ClientHandler", "onInputUpdate", "(Lnet/minecraft/util/MovementInput;)V", false);
//						Label l44 = new Label();
						if(ainNode instanceof MethodInsnNode)
						{
							MethodInsnNode min=(MethodInsnNode) ainNode;
							if(min.getOpcode()==Opcodes.INVOKEVIRTUAL&&(min.name.equals("updatePlayerMoveState")||(min.name.equals("a")&&min.desc.equals("()V"))))
							{
								insertPoint=ainNode.getNext();
								//Label
								InsnList ilist=new InsnList();
								LabelNode l0=new LabelNode();
								ilist.add(l0);
								ilist.add(new VarInsnNode(Opcodes.ALOAD,0));
								if(isObf)
								{
									ilist.add(new FieldInsnNode(Opcodes.GETFIELD, "blk", "c", "Lbli;"));
									ilist.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "indi/ma/client/ClientHandler", "onInputUpdate", "(Lbli;)V", false));
									
								}else {
									ilist.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "movementInput", "Lnet/minecraft/util/MovementInput;"));
									ilist.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "indi/ma/client/ClientHandler", "onInputUpdate", "(Lnet/minecraft/util/MovementInput;)V", false));
								}
								ilist.add(new LabelNode());
								methodNode.instructions.insertBefore(insertPoint, ilist);
								break;
							}
						}
					}
				}
				break;
			}
		}
		
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
        FMLLog.warning("[Transformed:net.minecraft.client.renderer.entity.EntityPlayerSP]");
        return classWriter.toByteArray();
	}
	public byte[] transformEntityRenderer(byte[] basicClass){
		FMLLog.warning("[Transforming:net.minecraft.client.renderer.EntityRenderer]");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		for(MethodNode methodNode : classNode.methods)
		{
			
			//System.out.println("P1");
			//System.out.println(methodNode.name);
			if(methodNode.name.equals("h")) //rotateCorpse
			{// (Lnet/minecraft/entity/EntityLivingBase;FFF)V //(Lsv;FFF)V
				//System.out.println("P2");
				if(methodNode.desc.equals("(F)V"))
				{
					//System.out.println("P3");
					System.out.println("h (F)V matched!");
					AbstractInsnNode insertPoint;
					for(int i=0;i<methodNode.instructions.size();i++)
					{
						AbstractInsnNode ainNode=methodNode.instructions.get(i);
//						ALOAD 0: this
//					    GETFIELD EntityRenderer.mc : Minecraft
//					    GETFIELD Minecraft.gameSettings : GameSettings
//					    GETFIELD GameSettings.debugCamEnable : boolean
//					    IFNE L58
//						L56
//					    LINENUMBER 647 L56
//					    ALOAD 2: entitylivingbase
//					    GETFIELD EntityLivingBase.prevRotationPitch : float
//					    ALOAD 2: entitylivingbase
//					    GETFIELD EntityLivingBase.rotationPitch : float
//					    ALOAD 2: entitylivingbase
//					    GETFIELD EntityLivingBase.prevRotationPitch : float
						
//						mv.visitVarInsn(ALOAD, 0);
//						mv.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/EntityRenderer", "mc", "Lnet/minecraft/client/Minecraft;");
//						mv.visitFieldInsn(GETFIELD, "net/minecraft/client/Minecraft", "gameSettings", "Lnet/minecraft/client/settings/GameSettings;");
//						mv.visitFieldInsn(GETFIELD, "net/minecraft/client/settings/GameSettings", "debugCamEnable", "Z");
//						Label l58 = new Label();
//						mv.visitJumpInsn(IFNE, l58);
						
						
						if(ainNode instanceof VarInsnNode
							&& methodNode.instructions.get(i+1) instanceof FieldInsnNode
							&& methodNode.instructions.get(i+2) instanceof FieldInsnNode
							&& methodNode.instructions.get(i+3) instanceof FieldInsnNode
							&& methodNode.instructions.get(i+4) instanceof JumpInsnNode
							&& methodNode.instructions.get(i+5) instanceof LabelNode
							&& methodNode.instructions.get(i+7) instanceof VarInsnNode
							&& ((VarInsnNode)methodNode.instructions.get(i+7)).var==2)
						{
							//System.out.println("7VarInsnNode: "+methodNode.instructions.get(i+7));
							FieldInsnNode fin1=(FieldInsnNode) methodNode.instructions.get(i+1);//o:blt  n:t d:Lbao;
							FieldInsnNode fin2=(FieldInsnNode) methodNode.instructions.get(i+2);//o:bao  n:u d:Lbbj;
							FieldInsnNode fin3=(FieldInsnNode) methodNode.instructions.get(i+3);//o:bbj  n:aC d:Z
							JumpInsnNode jin4=(JumpInsnNode) methodNode.instructions.get(i+4);
//							System.out.println("o:"+fin1.owner+"  n:"+fin1.name+" d:"+fin1.desc);
//							System.out.println("o:"+fin2.owner+"  n:"+fin2.name+" d:"+fin2.desc);
//							System.out.println("o:"+fin3.owner+"  n:"+fin3.name+" d:"+fin3.desc);
//							System.out.println("labelN: "+methodNode.instructions.indexOf(jin4.label));
//							System.out.println("insertPointCatched. ");
							insertPoint= ainNode;
//							FLOAD 1: p_78467_1_
//						    FLOAD 11: pitch
//						    FLOAD 10: yaw
//						    INVOKESTATIC ClientHandler.onCameraUpdate (float, float, float) : boolean
//						    IFNE L58
							
							
//						   L59
//						    LINENUMBER 645 L59
//						    ALOAD 0: this
//						    GETFIELD EntityRenderer.mc : Minecraft
//						    GETFIELD Minecraft.gameSettings : GameSettings
//						    GETFIELD GameSettings.debugCamEnable : boolean
//						    IFNE L58
							
							JumpInsnNode jin=(JumpInsnNode) methodNode.instructions.get(i+4);
							LabelNode l55=jin.label;
							
							InsnList ilist=new InsnList();
							LabelNode l0=new LabelNode();
							//ilist.add(l0);
							ilist.add(new VarInsnNode(Opcodes.ALOAD,2));
							ilist.add(new VarInsnNode(Opcodes.FLOAD,1));
							ilist.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"indi/ma/client/ClientHandler", "onCameraUpdate", "(Lsv;F)Z", false));
							ilist.add(new JumpInsnNode(Opcodes.IFNE,l55));
							ilist.add(l0);
							methodNode.instructions.insertBefore(insertPoint, ilist);
							//methodNode.instructions.insert(insertPoint, new LabelNode());
							break;
						}
					}
					break;
				}
			}
		}
		
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
        FMLLog.warning("[Transformed:net.minecraft.client.renderer.EntityRenderer]");
        return classWriter.toByteArray();
	}
	public byte[] transformRendererLivingEntity(byte[] basicClass)
	{
		FMLLog.warning("[Transforming:net.minecraft.client.renderer.entity.RendererLivingEntity]");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		for(MethodNode methodNode : classNode.methods)
		{
			//System.out.println("P1");
			//System.out.println(methodNode.name);
			//match?
			if(methodNode.name.equals("a")) //rotateCorpse
			{// (Lnet/minecraft/entity/EntityLivingBase;FFF)V //(Lsv;FFF)V
				//System.out.println("P2");
				if(methodNode.desc.equals("(Lsv;FFF)V"))
				{
					//System.out.println("P3");
					//System.out.println("a (Lsv;FFF)V matched!");
					LabelNode insertPoint;
					for(AbstractInsnNode ainNode : methodNode.instructions.toArray())
					{
						if(ainNode instanceof LabelNode)
						{
							insertPoint=(LabelNode) ainNode;
							//Label
							InsnList ilist=new InsnList();
							LabelNode l0=new LabelNode();
							ilist.add(l0);
							ilist.add(new VarInsnNode(Opcodes.ALOAD,1));
							ilist.add(new VarInsnNode(Opcodes.FLOAD,3));
							ilist.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"indi/ma/client/ClientHandler", "applyRotations", "(Lsv;F)Z", false));
							LabelNode l1=new LabelNode();
							ilist.add(new JumpInsnNode(Opcodes.IFEQ,l1));
							LabelNode l2=new LabelNode();
							ilist.add(l2);
							ilist.add(new InsnNode(Opcodes.RETURN));
							ilist.add(l1);
							ilist.add(new FrameNode(Opcodes.F_SAME,0, null, 0, null));
							
							
//							Label l0 = new Label();
//							mv.visitLabel(l0);
//							mv.visitLineNumber(347, l0);
//							mv.visitVarInsn(ALOAD, 1);
//							mv.visitVarInsn(FLOAD, 3);
//							mv.visitMethodInsn(INVOKESTATIC, "indi/ma/client/ClientHandler", "applyRotations", "(Lnet/minecraft/entity/EntityLivingBase;F)Z", false);
//							Label l1 = new Label();
//							mv.visitJumpInsn(IFEQ, l1);
//							Label l2 = new Label();
//							mv.visitLabel(l2);
//							mv.visitLineNumber(349, l2);
//							mv.visitInsn(RETURN);
//							mv.visitLabel(l1);
//							mv.visitLineNumber(351, l1);
//							mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
							
//							ALOAD 1: p_77043_1_
//						    FLOAD 3: p_77043_3_
//						    INVOKESTATIC ClientHandler.applyRotations (EntityLivingBase, float) : boolean
//						    IFEQ L1
//						   L2
//						    LINENUMBER 349 L2
//						    RETURN
//						   L1
//						    LINENUMBER 351 L1
//						   FRAME SAME
							
							
							methodNode.instructions.insertBefore(insertPoint, ilist);
							break;
						}
					}
					break;
				}
			}
		}
		
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
        FMLLog.warning("[Transformed:net.minecraft.client.renderer.entity.RendererLivingEntity]");
        return classWriter.toByteArray();
	}
}
