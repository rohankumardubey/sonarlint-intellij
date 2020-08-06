/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2020 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.FontPreferences;
import com.intellij.openapi.editor.impl.ComplementaryFontsRegistry;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UseJBColor")
public class SecondaryLocationIndexRenderer implements EditorCustomElementRenderer {
  private static final int HORIZONTAL_MARGIN = 3;
  private static final int HORIZONTAL_PADDING = 5;
  private static final int VERTICAL_PADDING = 3;
  private static final int ARC_RADIUS = 8;

  private static final Color LIGHT_BACKGROUND = new Color(0xd1, 0x85, 0x82);
  private static final Color LIGHT_SELECTED_BACKGROUND = new Color(0xa4, 0x03, 0x0f);

  private static final Color DARK_BACKGROUND = new Color(0x74, 0x23, 0x2f);
  private static final Color DARK_SELECTED_BACKGROUND = new Color(0xb4, 0x13, 0x1f);

  private static final JBColor BACKGROUND_JB_COLOR = new JBColor(LIGHT_BACKGROUND, DARK_BACKGROUND);
  private static final JBColor SELECTED_BACKGROUND_JB_COLOR = new JBColor(LIGHT_SELECTED_BACKGROUND, DARK_SELECTED_BACKGROUND);

  private static final JBColor INDEX_JB_COLOR = new JBColor(Color.WHITE, Color.LIGHT_GRAY);
  private static final JBColor SELECTED_INDEX_JB_COLOR = new JBColor(Color.WHITE, Color.WHITE);

  private final String index;
  private final boolean selected;

  public SecondaryLocationIndexRenderer(int index, boolean selected) {
    this.index = Integer.toString(index);
    this.selected = selected;
  }

  private static FontInfo getFontInfo(@NotNull Editor editor) {
    EditorColorsScheme colorsScheme = editor.getColorsScheme();
    FontPreferences fontPreferences = colorsScheme.getFontPreferences();
    return ComplementaryFontsRegistry.getFontAbleToDisplay('a', Font.PLAIN, fontPreferences,
      FontInfo.getFontRenderContext(editor.getContentComponent()));
  }

  @Override
  public int calcWidthInPixels(@NotNull Inlay inlay) {
    FontInfo fontInfo = getFontInfo(inlay.getEditor());
    return fontInfo.fontMetrics().stringWidth(index) + 2 * (HORIZONTAL_PADDING + HORIZONTAL_MARGIN);
  }

  @Override
  public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
    enableAntiAliasing(g);
    g.setColor(selected ? SELECTED_BACKGROUND_JB_COLOR : BACKGROUND_JB_COLOR);
    g.fillRoundRect(
      targetRegion.x + HORIZONTAL_MARGIN,
      targetRegion.y + VERTICAL_PADDING,
      targetRegion.width - 2 * HORIZONTAL_MARGIN,
      targetRegion.height - 2 * VERTICAL_PADDING,
      ARC_RADIUS,
      ARC_RADIUS
    );
    FontInfo fontInfo = getFontInfo(inlay.getEditor());
    g.setFont(fontInfo.getFont());
    g.setColor(selected ? SELECTED_INDEX_JB_COLOR : INDEX_JB_COLOR);
    g.drawString(index, targetRegion.x + HORIZONTAL_PADDING + HORIZONTAL_MARGIN, targetRegion.y + fontInfo.fontMetrics().getAscent() + 2);
  }

  /**
   * Enable antialiasing to get proper rounded corners. Without this, the bottom corners are not rounded.
   * @see <a href="https://stackoverflow.com/questions/4855847/problem-with-fillroundrect-seemingly-not-rendering-correctly">Answer on StackOverflow</a>
   * @param g the graphic context we are rendering to
   */
  private static void enableAntiAliasing(Graphics g) {
    if (g instanceof Graphics2D) {
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
  }
}
