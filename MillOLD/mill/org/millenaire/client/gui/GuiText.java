package org.millenaire.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.millenaire.common.MLN;
import org.millenaire.common.MLN.MillenaireException;

public abstract class GuiText extends GuiScreen {

	public static class Line {

		String text = "";
		MillGuiButton[] buttons = null;
		MillGuiTextField textField = null;
		List<ItemStack> icons = null;
		List<String> iconExtraLegends = null;
		boolean canCutAfter = true;
		boolean shadow = false;
		int margin = 0;

		public Line() {
		}

		public Line(final boolean canCutAfter) {
			this.canCutAfter = canCutAfter;
		}

		public Line(final List<ItemStack> icons, final List<String> iconExtraLegends, final String s, final int margin) {
			this.icons = icons;
			this.iconExtraLegends = iconExtraLegends;

			if (icons != null && iconExtraLegends == null) {
				MLN.printException("iconExtraLegends is null but icons isn't.", new Exception());
			} else if (icons != null && iconExtraLegends != null && icons.size() != iconExtraLegends.size()) {
				MLN.printException("iconExtraLegends has a size of " + iconExtraLegends.size() + " but icons has a size of " + icons.size(), new Exception());
			}

			text = s;
			canCutAfter = false;

			this.margin = margin;
		}

		public Line(final MillGuiButton b) {
			buttons = new MillGuiButton[] { b };
			canCutAfter = false;
		}

		public Line(final MillGuiButton b, final MillGuiButton b2) {
			buttons = new MillGuiButton[] { b, b2 };
			canCutAfter = false;
		}

		public Line(final MillGuiButton b, final MillGuiButton b2, final MillGuiButton b3) {
			buttons = new MillGuiButton[] { b, b2, b3 };
			canCutAfter = false;
		}

		public Line(final MillGuiTextField tf) {
			textField = tf;
		}

		public Line(final String s) {
			if (s == null) {
				text = "";
			} else {
				text = s;
				interpretTags();
			}
		}

		public Line(final String s, final boolean canCutAfter) {
			if (s == null) {
				text = "";
			} else {
				text = s;
				interpretTags();
			}
			this.canCutAfter = canCutAfter;
		}

		public Line(final String s, final Line model, final int lnpos) {

			if (model.icons != null && lnpos % 2 == 0) {

				final int lnicon = lnpos / 2;

				icons = new ArrayList<ItemStack>();
				iconExtraLegends = new ArrayList<String>();

				for (int i = lnicon * 4; i < model.icons.size() && i < (lnicon + 1) * 4; i++) {
					icons.add(model.icons.get(i));
					iconExtraLegends.add(model.iconExtraLegends.get(i));
				}
			}

			if (s == null) {
				text = "";
			} else {
				text = s;
				interpretTags();
			}
			canCutAfter = model.canCutAfter;
			shadow = model.shadow;
			margin = model.margin;
		}

		public Line(final String s, final MillGuiTextField tf) {
			textField = tf;
			if (s == null) {
				text = "";
			} else {
				text = s;
				interpretTags();
			}
		}

		public boolean empty() {
			return (text == null || text.length() == 0) && buttons == null && textField == null;
		}

		private void interpretTags() {
			if (text.startsWith("<shadow>")) {
				shadow = true;
				text = text.replaceAll("<shadow>", "");
			}
			text = text.replaceAll(BLACK, "\2470");
			text = text.replaceAll(DARKBLUE, "\2471");
			text = text.replaceAll(DARKGREEN, "\2472");
			text = text.replaceAll(LIGHTBLUE, "\2473");
			text = text.replaceAll(DARKRED, "\2474");
			text = text.replaceAll(PURPLE, "\2475");
			text = text.replaceAll(ORANGE, "\2476");
			text = text.replaceAll(LIGHTGREY, "\2477");
			text = text.replaceAll(DARKGREY, "\2478");
			text = text.replaceAll(BLUE, "\2479");
			text = text.replaceAll(LIGHTGREEN, "\247a");
			text = text.replaceAll(CYAN, "\247b");
			text = text.replaceAll(LIGHTRED, "\247c");
			text = text.replaceAll(PINK, "\247d");
			text = text.replaceAll(YELLOW, "\247e");
			text = text.replaceAll(WHITE, "\247f");

		}
	}

	public static class MillGuiButton extends GuiButton {

		public static final int HELPBUTTON = 2000;
		public static final int CHUNKBUTTON = 3000;
		public static final int CONFIGBUTTON = 4000;

		public MillGuiButton(final int par1, final int par2, final int par3, final int par4, final int par5, final String par6Str) {
			super(par1, par2, par3, par4, par5, par6Str);
		}

		public MillGuiButton(final String label, final int id) {
			super(id, 0, 0, 0, 0, label);
		}

		public int getHeight() {
			return height;
		}

		public int getWidth() {
			return width;
		}

		public void setHeight(final int h) {
			height = h;
		}

		public void setWidth(final int w) {
			width = w;
		}

	}

	public static class MillGuiTextField extends GuiTextField {

		public final String fieldKey;

		public MillGuiTextField(final FontRenderer par1FontRenderer, final int par2, final int par3, final int par4, final int par5, final String fieldKey) {
			super(par1FontRenderer, par2, par3, par4, par5);
			this.fieldKey = fieldKey;
		}
	}

	public static final String WHITE = "<white>";
	public static final String YELLOW = "<yellow>";
	public static final String PINK = "<pink>";
	public static final String LIGHTRED = "<lightred>";
	public static final String CYAN = "<cyan>";
	public static final String LIGHTGREEN = "<lightgreen>";
	public static final String BLUE = "<blue>";
	public static final String DARKGREY = "<darkgrey>";
	public static final String LIGHTGREY = "<lightgrey>";
	public static final String ORANGE = "<orange>";
	public static final String PURPLE = "<purple>";
	public static final String DARKRED = "<darkred>";
	public static final String LIGHTBLUE = "<lightblue>";
	public static final String DARKGREEN = "<darkgreen>";

	public static final String DARKBLUE = "<darkblue>";

	public static final String BLACK = "<black>";

	public static final String LINE_HELP_GUI_BUTTON = "<help_gui_button>";
	public static final String LINE_CHUNK_GUI_BUTTON = "<chunk_gui_button>";
	public static final String LINE_CONFIG_GUI_BUTTON = "<config_gui_button>";
	protected int pageNum = 0;

	protected List<List<Line>> descText = null;

	List<MillGuiTextField> textFields = new ArrayList<MillGuiTextField>();

	/** Stacks renderer. Icons, stack size, health, etc... */
	protected static RenderItem itemRenderer = new RenderItem();

	public GuiText() {

	}

	/**
	 * Utility method that splits lines that are longer than screen width and
	 * pages long than screen height
	 * 
	 * @param baseText
	 * @return
	 */
	public List<List<Line>> adjustText(final List<List<Line>> baseText) {
		final List<List<Line>> text = new ArrayList<List<Line>>();

		for (final List<Line> page : baseText) {

			final List<Line> newPage = new ArrayList<Line>();

			for (final Line line : page) {
				if (line.buttons != null || line.textField != null) {
					newPage.add(line);
				} else {
					for (String l : line.text.split("<ret>")) {

						final int lineSize = getLineSizeInPx() - line.margin;
						final int lineSizeInChar = getLineWidthInChars(lineSize);

						int lnpos = 0;

						while (fontRendererObj.getStringWidth(l) > lineSize) {
							int end = l.lastIndexOf(' ', lineSizeInChar);
							if (end < 1) {
								end = lineSizeInChar;
							}
							if (end >= l.length()) {
								end = l.length() / 2;
							}
							final String subLine = l.substring(0, end);
							l = l.substring(subLine.length()).trim();

							final int colPos = subLine.lastIndexOf('\247');

							if (colPos > -1) {// carrying over an open colour
												// tag
								l = subLine.substring(colPos, colPos + 2) + l;
							}

							newPage.add(new Line(subLine, line, lnpos));

							lnpos++;
						}
						newPage.add(new Line(l, line, lnpos));

						lnpos++;

						if (line.icons != null) {
							for (int i = lnpos; i < line.icons.size() / 2; i++) {
								newPage.add(new Line("", line, i));
							}
						}

					}
				}
			}

			while (newPage.size() > getPageSize()) {
				List<Line> newPage2 = new ArrayList<Line>();

				int nblinetaken = 0;

				for (int i = 0; i < getPageSize(); i++) {

					int blockSize = -1;

					for (int j = i; j < newPage.size() && blockSize == -1; j++) {
						if (newPage.get(j).canCutAfter) {
							blockSize = j - i;
						}
					}

					if (blockSize == -1) {
						blockSize = newPage.size() - i;
					}

					if (i + blockSize > getPageSize()) {
						break;
					}

					newPage2.add(newPage.get(i));
					nblinetaken++;
				}
				for (int i = 0; i < nblinetaken; i++) {
					newPage.remove(0);
				}

				newPage2 = clearEmptyLines(newPage2);

				if (newPage2 != null) {
					text.add(newPage2);
				}
			}

			final List<Line> adjustedPage = clearEmptyLines(newPage);

			if (adjustedPage != null) {
				text.add(adjustedPage);
			}
		}

		return text;
	}

	@SuppressWarnings("unchecked")
	public void buttonPagination() {

		try {

			if (descText == null) {
				return;
			}

			final int xStart = (width - getXSize()) / 2;
			final int yStart = (height - getYSize()) / 2;

			buttonList.clear();
			textFields.clear();

			int vpos = 6;

			if (pageNum < descText.size()) {
				for (int cp = 0; cp < getPageSize() && cp < descText.get(pageNum).size(); cp++) {

					final Line line = descText.get(pageNum).get(cp);
					if (line.buttons != null) {

						if (line.buttons.length == 1) {
							if (line.buttons[0] != null) {
								line.buttons[0].xPosition = xStart + getXSize() / 2 - 100;
								line.buttons[0].setWidth(200);
							}
						} else if (line.buttons.length == 2) {
							if (line.buttons[0] != null) {
								line.buttons[0].xPosition = xStart + getXSize() / 2 - 100;
								line.buttons[0].setWidth(95);
							}
							if (line.buttons[1] != null) {
								line.buttons[1].xPosition = xStart + getXSize() / 2 + 5;
								line.buttons[1].setWidth(95);
							}
						} else if (line.buttons.length == 3) {
							if (line.buttons[0] != null) {
								line.buttons[0].xPosition = xStart + getXSize() / 2 - 100;
								line.buttons[0].setWidth(60);
							}
							if (line.buttons[1] != null) {
								line.buttons[1].xPosition = xStart + getXSize() / 2 - 30;
								line.buttons[1].setWidth(60);
							}
							if (line.buttons[2] != null) {
								line.buttons[2].xPosition = xStart + getXSize() / 2 + 40;
								line.buttons[2].setWidth(60);
							}
						}

						for (int i = 0; i < line.buttons.length; i++) {
							if (line.buttons[i] != null) {
								line.buttons[i].yPosition = yStart + vpos;
								line.buttons[i].setHeight(20);
								buttonList.add(line.buttons[i]);
							}
						}
					} else if (line.textField != null) {
						final MillGuiTextField textField = new MillGuiTextField(fontRendererObj, xStart + getXSize() / 2 + 40, yStart + vpos, 95, 20, line.textField.fieldKey);
						textField.setText(line.textField.getText());
						textField.setMaxStringLength(line.textField.getMaxStringLength());
						textField.setTextColor(-1);
						line.textField = textField;
						line.textField.setTextColor(-1);
						line.textField.setEnableBackgroundDrawing(false);

						textFields.add(textField);
					}
					vpos += 10;
				}
			}
		} catch (final Exception e) {
			MLN.printException("Exception while doing button pagination in GUI " + this, e);
		}
	}

	private List<Line> clearEmptyLines(final List<Line> page) {
		final List<Line> clearedPage = new ArrayList<Line>();

		boolean nonEmptyLine = false;

		for (final Line line : page) {
			if (!line.empty()) {
				clearedPage.add(line);
				nonEmptyLine = true;
			} else {
				if (nonEmptyLine) {
					clearedPage.add(line);
				}
			}
		}

		if (clearedPage.size() > 0) {
			return clearedPage;
		} else {
			return null;
		}

	}

	protected void closeWindow() {
		mc.displayGuiScreen(null);
		mc.setIngameFocus();
	}

	public List<List<Line>> convertAdjustText(final List<List<String>> baseText) {

		final List<List<Line>> text = new ArrayList<List<Line>>();

		for (final List<String> page : baseText) {

			final List<Line> newPage = new ArrayList<Line>();

			for (final String s : page) {
				if (s.equals(LINE_HELP_GUI_BUTTON)) {
					newPage.add(new Line(new MillGuiButton(MillGuiButton.HELPBUTTON, 0, 0, 0, 0, MLN.string("ui.helpbutton"))));
				} else if (s.equals(LINE_CHUNK_GUI_BUTTON)) {
					newPage.add(new Line(new MillGuiButton(MillGuiButton.CHUNKBUTTON, 0, 0, 0, 0, MLN.string("ui.chunkbutton"))));
				} else if (s.equals(LINE_CONFIG_GUI_BUTTON)) {
					newPage.add(new Line(new MillGuiButton(MillGuiButton.CONFIGBUTTON, 0, 0, 0, 0, MLN.string("ui.configbutton"))));
				} else {
					newPage.add(new Line(s, true));
				}
			}

			text.add(newPage);
		}

		return adjustText(text);
	}

	protected abstract void customDrawBackground(int i, int j, float f);

	protected abstract void customDrawScreen(int i, int j, float f);

	public void decrementPage() {

		if (descText == null) {
			return;
		}

		if (pageNum > 0) {
			pageNum--;
		}
		buttonPagination();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	protected void drawHoveringText(final List par1List, final int par2, final int par3, final FontRenderer font) {
		if (!par1List.isEmpty()) {
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;
			final Iterator iterator = par1List.iterator();

			while (iterator.hasNext()) {
				final String s = (String) iterator.next();
				final int l = font.getStringWidth(s);

				if (l > k) {
					k = l;
				}
			}

			int i1 = par2 + 12;
			int j1 = par3 - 12;
			int k1 = 8;

			if (par1List.size() > 1) {
				k1 += 2 + (par1List.size() - 1) * 10;
			}

			if (i1 + k > this.width) {
				i1 -= 28 + k;
			}

			if (j1 + k1 + 6 > this.height) {
				j1 = this.height - k1 - 6;
			}

			this.zLevel = 300.0F;
			itemRenderer.zLevel = 300.0F;
			final int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			final int i2 = 1347420415;
			final int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for (int k2 = 0; k2 < par1List.size(); ++k2) {
				final String s1 = (String) par1List.get(k2);
				font.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0) {
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			itemRenderer.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void drawItemStackTooltip(final ItemStack par1ItemStack, final int par2, final int par3, final String extraLegend) {
		final List list = par1ItemStack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

		if (extraLegend != null) {
			list.add(extraLegend);
		}

		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, par1ItemStack.getRarity().rarityColor + (String) list.get(k));
			} else {
				list.set(k, EnumChatFormatting.GRAY + (String) list.get(k));
			}
		}

		final FontRenderer font = par1ItemStack.getItem().getFontRenderer(par1ItemStack);
		drawHoveringText(list, par2, par3, font == null ? fontRendererObj : font);
	}

	@Override
	public void drawScreen(final int i, final int j, final float f) {

		try {

			drawDefaultBackground();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.renderEngine.bindTexture(getPNGPath());
			final int xStart = (width - getXSize()) / 2;
			final int yStart = (height - getYSize()) / 2;
			drawTexturedModalRect(xStart, yStart, 0, 0, getXSize(), getYSize());

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			customDrawBackground(i, j, f);

			GL11.glPushMatrix();
			GL11.glRotatef(180F, 1.0F, 0.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glTranslatef(xStart, yStart, 0.0F);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(2896 /* GL_LIGHTING */);
			GL11.glDisable(2929 /* GL_DEPTH_TEST */);

			if (descText != null) {
				int vpos = 6;

				if (pageNum < descText.size()) {

					if (descText.get(pageNum) == null) {
						MLN.printException(new MillenaireException("descText.get(pageNum)==null for pageNum: " + pageNum + " in GUI: " + this));
					}

					for (int cp = 0; cp < getPageSize() && cp < descText.get(pageNum).size(); cp++) {

						if (descText.get(pageNum).get(cp).shadow) {
							fontRendererObj.drawStringWithShadow(descText.get(pageNum).get(cp).text, getTextXStart() + descText.get(pageNum).get(cp).margin, vpos, 0x101010);
						} else {
							fontRendererObj.drawString(descText.get(pageNum).get(cp).text, getTextXStart() + descText.get(pageNum).get(cp).margin, vpos, 0x101010);
						}

						vpos += 10;
					}
				}

				fontRendererObj.drawString(pageNum + 1 + "/" + getNbPage(), getXSize() / 2 - 10, getYSize() - 10, 0x101010);

				vpos = 6;

				this.zLevel = 100.0F;
				itemRenderer.zLevel = 100.0F;

				ItemStack hoverIcon = null;
				String extraLegend = null;

				if (pageNum < descText.size()) {
					for (int cp = 0; cp < getPageSize() && cp < descText.get(pageNum).size(); cp++) {
						if (descText.get(pageNum).get(cp).icons != null) {
							for (int ic = 0; ic < descText.get(pageNum).get(cp).icons.size(); ic++) {
								final ItemStack icon = descText.get(pageNum).get(cp).icons.get(ic);

								if (descText.get(pageNum).get(cp).iconExtraLegends == null) {
									MLN.error(null, "Null legends!");
								}

								final String legend = descText.get(pageNum).get(cp).iconExtraLegends.get(ic);

								GL11.glEnable(GL11.GL_DEPTH_TEST);
								itemRenderer.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.renderEngine, icon, getTextXStart() + 18 * ic, vpos);
								itemRenderer.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.renderEngine, icon, getTextXStart() + 18 * ic, vpos, null);

								if (xStart + getTextXStart() + 18 * ic < i && yStart + vpos < j && xStart + getTextXStart() + 18 * ic + 16 > i && yStart + vpos + 16 > j) {
									hoverIcon = icon;
									extraLegend = legend;
								}
							}
						}
						vpos += 10;
					}
				}

				if (hoverIcon != null) {
					drawItemStackTooltip(hoverIcon, i - xStart, j - yStart, extraLegend);
				}

				itemRenderer.zLevel = 0.0F;
				this.zLevel = 0.0F;

				customDrawScreen(i, j, f);
			}

			GL11.glPopMatrix();
			super.drawScreen(i, j, f);
			GL11.glEnable(2896 /* GL_LIGHTING */);
			GL11.glEnable(2929 /* GL_DEPTH_TEST */);

			GL11.glDisable(GL11.GL_LIGHTING);

			for (final MillGuiTextField textField : textFields) {
				textField.drawTextBox();
			}
		} catch (final Exception e) {
			MLN.printException("Exception in drawScreen of GUI: " + this, e);
		}
	}

	public abstract int getLineSizeInPx();

	private int getLineWidthInChars(final int lineWidthInPx) {
		String testLine = "a";

		while (fontRendererObj.getStringWidth(testLine) < lineWidthInPx) {
			testLine += "a";
		}

		return testLine.length() - 1;
	}

	protected int getNbPage() {
		return descText.size();
	}

	public abstract int getPageSize();

	public abstract ResourceLocation getPNGPath();

	public int getTextXStart() {
		return 8;
	}

	public abstract int getXSize();

	public abstract int getYSize();

	protected void handleTextFieldPress(final MillGuiTextField textField) {

	}

	public void incrementPage() {

		if (descText == null) {
			return;
		}

		if (pageNum < getNbPage() - 1) {
			pageNum++;
		}
		buttonPagination();
	}

	public abstract void initData();

	@Override
	public void initGui() {
		super.initGui();

		initData();

		buttonPagination();
	}

	@Override
	protected void keyTyped(final char c, final int i) {

		boolean keyTyped = false;
		for (final MillGuiTextField textField : textFields) {
			if (textField.textboxKeyTyped(c, i)) {
				keyTyped = true;
				handleTextFieldPress(textField);
			}
		}

		if (!keyTyped && i == 1) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

	@Override
	protected void mouseClicked(final int i, final int j, final int k) {

		final int xStart = (width - getXSize()) / 2;
		final int yStart = (height - getYSize()) / 2;

		final int ai = i - xStart;
		final int aj = j - yStart;

		if (aj > getYSize() - 14 && aj < getYSize()) {
			if (ai > 0 && ai < 33) {
				decrementPage();
			} else if (ai > getXSize() - 33 && ai < getXSize()) {
				incrementPage();
			}
		}

		for (final MillGuiTextField textField : textFields) {
			textField.mouseClicked(i, j, k);
		}

		super.mouseClicked(i, j, k);
	}

}
