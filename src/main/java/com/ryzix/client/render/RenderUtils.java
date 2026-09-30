package com.ryzix.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

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

		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2.0f);

		matrices.push();
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f matrix = entry.getPositionMatrix();

		// Draw filled faces with low alpha
		float fa = a * 0.25f;
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
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
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		// Bottom edges
		line(buffer, entry, x1, y1, z1, x2, y1, z1, r, g, b, a);
		line(buffer, entry, x2, y1, z1, x2, y1, z2, r, g, b, a);
		line(buffer, entry, x2, y1, z2, x1, y1, z2, r, g, b, a);
		line(buffer, entry, x1, y1, z2, x1, y1, z1, r, g, b, a);
		// Top edges
		line(buffer, entry, x1, y2, z1, x2, y2, z1, r, g, b, a);
		line(buffer, entry, x2, y2, z1, x2, y2, z2, r, g, b, a);
		line(buffer, entry, x2, y2, z2, x1, y2, z2, r, g, b, a);
		line(buffer, entry, x1, y2, z2, x1, y2, z1, r, g, b, a);
		// Vertical edges
		line(buffer, entry, x1, y1, z1, x1, y2, z1, r, g, b, a);
		line(buffer, entry, x2, y1, z1, x2, y2, z1, r, g, b, a);
		line(buffer, entry, x2, y1, z2, x2, y2, z2, r, g, b, a);
		line(buffer, entry, x1, y1, z2, x1, y2, z2, r, g, b, a);
		tessellator.draw();

		matrices.pop();

		RenderSystem.lineWidth(1.0f);
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void line(BufferBuilder buffer, MatrixStack.Entry entry,
							 float x1, float y1, float z1,
							 float x2, float y2, float z2,
							 float r, float g, float b, float a) {
		Matrix4f matrix = entry.getPositionMatrix();
		float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0f) return;
		dx /= len; dy /= len; dz /= len;
		// Lines shader expands each segment using the normal as its direction
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), dx, dy, dz).next();
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(entry.getNormalMatrix(), dx, dy, dz).next();
	}
}
