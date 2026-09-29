package com.ryzix.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

	public static void drawBox(MatrixStack matrices, Box box, float r, float g, float b, float a) {
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		Vec3d camPos = camera.getPos();

		float x1 = (float) (box.minX - camPos.x);
		float y1 = (float) (box.minY - camPos.y);
		float z1 = (float) (box.minZ - camPos.z);
		float x2 = (float) (box.maxX - camPos.x);
		float y2 = (float) (box.maxY - camPos.y);
		float z2 = (float) (box.maxZ - camPos.z);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		RenderSystem.disableTexture();
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2.0f);

		matrices.push();
		Matrix4f matrix = matrices.peek().getModel();

		// Draw filled faces with low alpha
		float fa = a * 0.25f;
		buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
		// Bottom
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y1, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y1, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y1, z2).color(r, g, b, fa).next();
		// Top
		buffer.vertex(matrix, x1, y2, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y2, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y2, z1).color(r, g, b, fa).next();
		// North
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y2, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y2, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y1, z1).color(r, g, b, fa).next();
		// South
		buffer.vertex(matrix, x1, y1, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y1, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y2, z2).color(r, g, b, fa).next();
		// West
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y1, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y2, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x1, y2, z1).color(r, g, b, fa).next();
		// East
		buffer.vertex(matrix, x2, y1, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y2, z1).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, fa).next();
		buffer.vertex(matrix, x2, y1, z2).color(r, g, b, fa).next();
		tessellator.draw();

		// Draw outline edges
		buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
		// Bottom edges
		line(buffer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
		line(buffer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
		line(buffer, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
		line(buffer, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);
		// Top edges
		line(buffer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
		line(buffer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
		line(buffer, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
		line(buffer, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);
		// Vertical edges
		line(buffer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
		line(buffer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
		line(buffer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
		line(buffer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
		tessellator.draw();

		matrices.pop();

		RenderSystem.enableTexture();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void line(BufferBuilder buffer, Matrix4f matrix,
							 float x1, float y1, float z1,
							 float x2, float y2, float z2,
							 float r, float g, float b, float a) {
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
	}
}
