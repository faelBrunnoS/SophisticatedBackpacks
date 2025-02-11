package net.p3pp3rf1y.sophisticatedbackpacks.client.gui.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.controls.ToggleButton;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiHelper {
	public static final ResourceLocation GUI_CONTROLS = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/gui_controls.png");
	private static final int GUI_CONTROLS_TEXTURE_WIDTH = 256;
	private static final int GUI_CONTROLS_TEXTURE_HEIGHT = 256;
	public static final TextureBlitData BAR_BACKGROUND_BOTTOM = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 66), Dimension.SQUARE_18);
	public static final TextureBlitData BAR_BACKGROUND_MIDDLE = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 48), Dimension.SQUARE_18);
	public static final TextureBlitData BAR_BACKGROUND_TOP = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 30), Dimension.SQUARE_18);
	public static final ResourceLocation ICONS = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/icons.png");
	public static final TextureBlitData CRAFTING_RESULT_SLOT = new TextureBlitData(GUI_CONTROLS, new UV(71, 216), new Dimension(26, 26));
	public static final TextureBlitData DEFAULT_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(47, 0), Dimension.SQUARE_18);
	public static final TextureBlitData DEFAULT_BUTTON_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(29, 0), Dimension.SQUARE_18);
	public static final TextureBlitData SMALL_BUTTON_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 18), Dimension.SQUARE_12);
	public static final TextureBlitData SMALL_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(41, 18), Dimension.SQUARE_12);
	public static final ResourceLocation SLOTS_BACKGROUND = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/slots_background.png");

	private static final Map<Integer, TextureBlitData> SLOTS_BACKGROUNDS = new HashMap<>();

	private GuiHelper() {}

	public static void renderItemInGUI(MatrixStack matrixStack, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition) {
		renderItemInGUI(matrixStack, minecraft, stack, xPosition, yPosition, false);
	}

	public static void renderSlotsBackground(Minecraft minecraft, MatrixStack matrixStack, int x, int y, int slotWidth, int slotHeight) {
		int key = getSlotsBackgroundKey(slotWidth, slotHeight);
		blit(minecraft, matrixStack, x, y, SLOTS_BACKGROUNDS.computeIfAbsent(key, k ->
				new TextureBlitData(SLOTS_BACKGROUND, Dimension.SQUARE_256, new UV(0, 0), new Dimension(slotWidth * 18, slotHeight * 18))
		));
	}

	private static int getSlotsBackgroundKey(int slotWidth, int slotHeight) {
		return slotWidth * 31 + slotHeight;
	}

	public static void renderItemInGUI(MatrixStack matrixStack, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition, boolean renderOverlay) {
		renderItemInGUI(matrixStack, minecraft, stack, xPosition, yPosition, renderOverlay, null);
	}

	public static void renderItemInGUI(MatrixStack matrixStack, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition, boolean renderOverlay,
			@Nullable String countText) {
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		float originalZLevel = itemRenderer.blitOffset;
		itemRenderer.blitOffset += getZOffset(matrixStack);
		itemRenderer.renderAndDecorateItem(stack, xPosition, yPosition);
		if (renderOverlay) {
			itemRenderer.renderGuiItemDecorations(minecraft.font, stack, xPosition, yPosition, countText);
		}
		itemRenderer.blitOffset = originalZLevel;
	}

	private static int getZOffset(MatrixStack matrixStack) {
		Float zOffset = ObfuscationReflectionHelper.getPrivateValue(Matrix4f.class, matrixStack.last().pose(), "field_226586_l_");
		return zOffset == null ? 0 : zOffset.intValue();
	}

	public static void blit(Minecraft minecraft, MatrixStack matrixStack, int x, int y, TextureBlitData texData) {
		minecraft.getTextureManager().bind(texData.getTextureName());
		AbstractGui.blit(matrixStack, x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(), texData.getWidth(), texData.getHeight(), texData.getTextureWidth(), texData.getTextureHeight());
	}

	public static void coloredBlit(Matrix4f matrix, int x, int y, TextureBlitData texData, int color) {
		float red = (color >> 16 & 255) / 255F;
		float green = (color >> 8 & 255) / 255F;
		float blue = (color & 255) / 255F;
		float alpha = (color >> 24 & 255) / 255F;

		int xMin = x + texData.getXOffset();
		int yMin = y + texData.getYOffset();
		int xMax = xMin + texData.getWidth();
		int yMax = yMin + texData.getHeight();

		float minU = (float) texData.getU() / texData.getTextureWidth();
		float maxU = minU + ((float) texData.getWidth() / texData.getTextureWidth());
		float minV = (float) texData.getV() / texData.getTextureHeight();
		float maxV = minV + ((float) texData.getHeight() / texData.getTextureWidth());

		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferbuilder.vertex(matrix, xMin, yMax, 0).color(red, green, blue, alpha).uv(minU, maxV).endVertex();
		bufferbuilder.vertex(matrix, xMax, yMax, 0).color(red, green, blue, alpha).uv(maxU, maxV).endVertex();
		bufferbuilder.vertex(matrix, xMax, yMin, 0).color(red, green, blue, alpha).uv(maxU, minV).endVertex();
		bufferbuilder.vertex(matrix, xMin, yMin, 0).color(red, green, blue, alpha).uv(minU, minV).endVertex();
		bufferbuilder.end();
		WorldVertexBufferUploader.end(bufferbuilder);
	}

	private static List<? extends ITextProperties> tooltipToRender = Collections.emptyList();

	public static void setTooltipToRender(List<? extends ITextProperties> tooltip) {
		tooltipToRender = tooltip;
	}

	public static void renderTooltip(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (tooltipToRender.isEmpty()) {
			return;
		}

		renderTooltip(minecraft, matrixStack, tooltipToRender, mouseX, mouseY, ITooltipRenderPart.EMPTY, null, ItemStack.EMPTY, 200);
		tooltipToRender = Collections.emptyList();
	}

	public static void renderTooltip(Minecraft minecraft, MatrixStack matrixStack, List<? extends ITextProperties> textLines, int mouseX, int mouseY,
			ITooltipRenderPart additionalRender, @Nullable FontRenderer tooltipRenderFont, ItemStack stack) {
		renderTooltip(minecraft, matrixStack, textLines, mouseX, mouseY, additionalRender, tooltipRenderFont, stack, 0);
	}

	public static void renderTooltip(Minecraft minecraft, MatrixStack matrixStack, List<? extends ITextProperties> textLines, int mouseX, int mouseY,
			ITooltipRenderPart additionalRender, @Nullable FontRenderer tooltipRenderFont, ItemStack stack, int maxTextWidth) {

		FontRenderer font = tooltipRenderFont == null ? minecraft.font : tooltipRenderFont;

		int windowWidth = minecraft.getWindow().getGuiScaledWidth();
		int windowHeight = minecraft.getWindow().getGuiScaledHeight();

		int tooltipWidth = getMaxLineWidth(textLines, font);

		if (maxTextWidth > 0 && tooltipWidth > maxTextWidth) {
			tooltipWidth = maxTextWidth;
		}

		int wrappedTooltipWidth = 0;
		List<ITextProperties> wrappedTextLines = new ArrayList<>();
		for (ITextProperties textLine : textLines) {
			List<ITextProperties> wrappedLine = font.getSplitter().splitLines(textLine, tooltipWidth, Style.EMPTY);

			for (ITextProperties line : wrappedLine) {
				int lineWidth = font.width(line);
				if (lineWidth > wrappedTooltipWidth) {wrappedTooltipWidth = lineWidth;}
				wrappedTextLines.add(line);
			}
		}
		tooltipWidth = wrappedTooltipWidth;
		tooltipWidth = Math.max(tooltipWidth, additionalRender.getWidth());

		textLines = wrappedTextLines;

		int leftX = mouseX + 12;
		if (leftX + tooltipWidth > windowWidth) {
			leftX -= 28 + tooltipWidth;
		}

		int topY = mouseY - 12;
		int tooltipHeight = 8;
		if (textLines.size() > 1) {
			tooltipHeight += 2 + (textLines.size() - 1) * 10;
		}
		tooltipHeight += additionalRender.getHeight();

		if (topY + tooltipHeight + 6 > windowHeight) {
			topY = windowHeight - tooltipHeight - 6;
		}

		int backgroundColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
		int borderColorStart = GuiUtils.DEFAULT_BORDER_COLOR_START;
		int borderColorEnd = GuiUtils.DEFAULT_BORDER_COLOR_END;
		RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, textLines, matrixStack, leftX, topY, font, backgroundColor, borderColorStart, borderColorEnd);
		MinecraftForge.EVENT_BUS.post(colorEvent);
		backgroundColor = colorEvent.getBackground();
		borderColorStart = colorEvent.getBorderStart();
		borderColorEnd = colorEvent.getBorderEnd();

		matrixStack.pushPose();
		Matrix4f matrix4f = matrixStack.last().pose();
		renderTooltipBackground(matrix4f, tooltipWidth, leftX, topY, tooltipHeight, backgroundColor, borderColorStart, borderColorEnd);

		MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, matrixStack, leftX, topY, font, tooltipWidth, tooltipHeight));

		IRenderTypeBuffer.Impl renderTypeBuffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
		matrixStack.translate(0.0D, 0.0D, 400.0D);

		topY = writeTooltipLines(textLines, font, leftX, topY, matrix4f, renderTypeBuffer, -1);

		renderTypeBuffer.endBatch();
		additionalRender.render(matrixStack, leftX, topY, font);
		matrixStack.popPose();

		MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, matrixStack, leftX, topY, font, tooltipWidth, tooltipHeight));
	}

	public static void renderTooltipBackground(Matrix4f matrix4f, int tooltipWidth, int leftX, int topY, int tooltipHeight, int backgroundColor, int borderColorStart, int borderColorEnd) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 4, leftX + tooltipWidth + 3, topY - 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY + tooltipHeight + 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 4, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 4, topY - 3, leftX - 3, topY + tooltipHeight + 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX + tooltipWidth + 3, topY - 3, leftX + tooltipWidth + 4, topY + tooltipHeight + 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 3 + 1, leftX - 3 + 1, topY + tooltipHeight + 3 - 1, 400, borderColorStart, borderColorEnd);
		fillGradient(matrix4f, bufferbuilder, leftX + tooltipWidth + 2, topY - 3 + 1, leftX + tooltipWidth + 3, topY + tooltipHeight + 3 - 1, 400, borderColorStart, borderColorEnd);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY - 3 + 1, 400, borderColorStart, borderColorStart);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY + tooltipHeight + 2, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, 400, borderColorEnd, borderColorEnd);
		RenderSystem.enableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		bufferbuilder.end();
		WorldVertexBufferUploader.end(bufferbuilder);
		RenderSystem.shadeModel(7424);
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	private static int getMaxLineWidth(List<? extends ITextProperties> tooltips, FontRenderer font) {
		int maxLineWidth = 0;
		for (ITextProperties line : tooltips) {
			int lineWidth = font.width(line);
			if (lineWidth > maxLineWidth) {
				maxLineWidth = lineWidth;
			}
		}
		return maxLineWidth;
	}

	public static int writeTooltipLines(List<? extends ITextProperties> textLines, FontRenderer font, float leftX, int topY, Matrix4f matrix4f, IRenderTypeBuffer.Impl renderTypeBuffer, int color) {
		for (int i = 0; i < textLines.size(); ++i) {
			ITextProperties line = textLines.get(i);
			if (line != null) {
				font.drawInBatch(LanguageMap.getInstance().getVisualOrder(line), leftX, topY, color, true, matrix4f, renderTypeBuffer, false, 0, 15728880);
			}

			if (i == 0) {
				topY += 2;
			}

			topY += 10;
		}
		return topY;
	}

	private static void fillGradient(Matrix4f matrix, BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int colorA, int colorB) {
		float f = (colorA >> 24 & 255) / 255.0F;
		float f1 = (colorA >> 16 & 255) / 255.0F;
		float f2 = (colorA >> 8 & 255) / 255.0F;
		float f3 = (colorA & 255) / 255.0F;
		float f4 = (colorB >> 24 & 255) / 255.0F;
		float f5 = (colorB >> 16 & 255) / 255.0F;
		float f6 = (colorB >> 8 & 255) / 255.0F;
		float f7 = (colorB & 255) / 255.0F;
		builder.vertex(matrix, x2, y1, z).color(f1, f2, f3, f).endVertex();
		builder.vertex(matrix, x1, y1, z).color(f1, f2, f3, f).endVertex();
		builder.vertex(matrix, x1, y2, z).color(f5, f6, f7, f4).endVertex();
		builder.vertex(matrix, x2, y2, z).color(f5, f6, f7, f4).endVertex();
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, ITextComponent... tooltip) {
		return getButtonStateData(uv, dimension, offset, Arrays.asList(tooltip));
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, String tooltip, Dimension dimension) {
		return getButtonStateData(uv, tooltip, dimension, new Position(0, 0));
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, String tooltip, Dimension dimension, Position offset) {
		return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension),
				new TranslationTextComponent(tooltip)
		);
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, List<? extends ITextComponent> tooltip) {
		return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension), tooltip);
	}

	public static void renderSlotsBackground(Minecraft minecraft, MatrixStack matrixStack, int x, int y, int slotsInRow, int fullSlotRows, int extraRowSlots) {
		renderSlotsBackground(minecraft, matrixStack, x, y, slotsInRow, fullSlotRows);
		if (extraRowSlots > 0) {
			renderSlotsBackground(minecraft, matrixStack, x, y + fullSlotRows * 18, extraRowSlots, 1);
		}
	}

	public static void renderTiledFluidTextureAtlas(MatrixStack matrixStack, TextureAtlasSprite sprite, int color, int x, int y, int height, Minecraft minecraft) {
		minecraft.getTextureManager().bind(sprite.atlas().location());
		BufferBuilder builder = Tessellator.getInstance().getBuilder();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);

		float u1 = sprite.getU0();
		float v1 = sprite.getV0();
		int spriteHeight = sprite.getHeight();
		int spriteWidth = sprite.getWidth();
		int startY = y;
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		do {
			int renderHeight = Math.min(spriteHeight, height);
			height -= renderHeight;
			float v2 = sprite.getV((16f * renderHeight) / spriteHeight);

			// we need to draw the quads per width too
			Matrix4f matrix = matrixStack.last().pose();
			float u2 = sprite.getU((16f * 16) / spriteWidth);
			builder.vertex(matrix, x, (float) startY + renderHeight, 100).color(red, green, blue, 1).uv(u1, v2).endVertex();
			builder.vertex(matrix, (float) x + 16, (float) startY + renderHeight, 100).color(red, green, blue, 1).uv(u2, v2).endVertex();
			builder.vertex(matrix, (float) x + 16, startY, 100).color(red, green, blue, 1).uv(u2, v1).endVertex();
			builder.vertex(matrix, x, startY, 100).color(red, green, blue, 1).uv(u1, v1).endVertex();

			startY += renderHeight;
		} while (height > 0);

		// finish drawing sprites
		builder.end();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.end(builder);
	}

	public static void renderControlBackground(MatrixStack matrixStack, Minecraft minecraft, int x, int y, int renderWidth, int renderHeight) {
		minecraft.getTextureManager().bind(GUI_CONTROLS);

		int u = 29;
		int v = 146;
		int textureBgWidth = 66;
		int textureBgHeight = 56;
		int halfWidth = renderWidth / 2;
		int halfHeight = renderHeight / 2;
		AbstractGui.blit(matrixStack, x, y, u, v, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		AbstractGui.blit(matrixStack, x, y + halfHeight, u, (float) v + textureBgHeight - halfHeight, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		AbstractGui.blit(matrixStack, x + halfWidth, y, (float) u + textureBgWidth - halfWidth, v, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
		AbstractGui.blit(matrixStack, x + halfWidth, y + halfHeight, (float) u + textureBgWidth - halfWidth, (float) v + textureBgHeight - halfHeight, halfWidth, halfHeight, GUI_CONTROLS_TEXTURE_WIDTH, GUI_CONTROLS_TEXTURE_HEIGHT);
	}

	public interface ITooltipRenderPart {
		ITooltipRenderPart EMPTY = new ITooltipRenderPart() {
			@Override
			public int getWidth() {
				return 0;
			}

			@Override
			public int getHeight() {
				return 0;
			}

			@Override
			public void render(MatrixStack matrixStack, int leftX, int topY, FontRenderer font) {
				//noop
			}
		};

		int getWidth();

		int getHeight();

		void render(MatrixStack matrixStack, int leftX, int topY, FontRenderer font);
	}

	public static void tryRenderGuiItem(ItemRenderer itemRenderer, TextureManager textureManager,
			@Nullable LivingEntity livingEntity, ItemStack stack, int x, int y, int rotation) {
		if (!stack.isEmpty()) {
			itemRenderer.blitOffset += 50.0F;

			try {
				renderGuiItem(itemRenderer, textureManager, stack, x, y, itemRenderer.getModel(stack, null, livingEntity), rotation);
			}
			catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
				CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
				crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
				crashreportcategory.setDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
				crashreportcategory.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
				crashreportcategory.setDetail("Item NBT", () -> String.valueOf(stack.getTag()));
				crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
				throw new ReportedException(crashreport);
			}

			itemRenderer.blitOffset -= 50.0F;
		}
	}

	private static void renderGuiItem(ItemRenderer itemRenderer, TextureManager textureManager, ItemStack pStack, int pX, int pY, IBakedModel pBakedmodel, int rotation) {
		RenderSystem.pushMatrix();
		textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
		textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.translatef((float) pX, (float) pY, 100.0F + itemRenderer.blitOffset);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		if (rotation != 0) {
			RenderSystem.rotatef(rotation, 0, 0, 1);
		}
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		MatrixStack matrixstack = new MatrixStack();
		IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
		boolean flag = !pBakedmodel.usesBlockLight();
		if (flag) {
			RenderHelper.setupForFlatItems();
		}

		itemRenderer.render(pStack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, pBakedmodel);
		irendertypebuffer$impl.endBatch();
		RenderSystem.enableDepthTest();
		if (flag) {
			RenderHelper.setupFor3DItems();
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
	}
}
