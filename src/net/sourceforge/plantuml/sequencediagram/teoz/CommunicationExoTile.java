/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2014, Arnaud Roques
 *
 * Project Info:  http://plantuml.sourceforge.net
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * Original Author:  Arnaud Roques
 * 
 * Revision $Revision: 4636 $
 *
 */
package net.sourceforge.plantuml.sequencediagram.teoz;

import java.awt.geom.Dimension2D;

import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.real.Real;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.MessageExo;
import net.sourceforge.plantuml.skin.Area;
import net.sourceforge.plantuml.skin.ArrowComponent;
import net.sourceforge.plantuml.skin.ArrowConfiguration;
import net.sourceforge.plantuml.skin.Component;
import net.sourceforge.plantuml.skin.ComponentType;
import net.sourceforge.plantuml.skin.Context2D;
import net.sourceforge.plantuml.skin.Skin;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class CommunicationExoTile implements TileWithUpdateStairs {

	private final LivingSpace livingSpace;
	private final MessageExo message;
	private final Skin skin;
	private final ISkinParam skinParam;
	private final TileArguments tileArguments;

	public Event getEvent() {
		return message;
	}

	public CommunicationExoTile(LivingSpace livingSpace, MessageExo message, Skin skin, ISkinParam skinParam,
			TileArguments tileArguments) {
		this.tileArguments = tileArguments;
		this.livingSpace = livingSpace;
		this.message = message;
		this.skin = skin;
		this.skinParam = skinParam;
	}

	private Component getComponent(StringBounder stringBounder) {
		ArrowConfiguration arrowConfiguration = message.getArrowConfiguration();
		if (message.getType().getDirection() == -1) {
			arrowConfiguration = arrowConfiguration.reverse();
		}
		final Component comp = skin.createComponent(ComponentType.ARROW, arrowConfiguration, skinParam,
				message.getLabel());
		return comp;
	}

	public void drawU(UGraphic ug) {
		final StringBounder stringBounder = ug.getStringBounder();
		final Component comp = getComponent(stringBounder);
		final Dimension2D dim = comp.getPreferredDimension(stringBounder);
		double x1 = getPoint1Value(stringBounder);
		double x2 = getPoint2(stringBounder).getCurrentValue();
		final int level = livingSpace.getLevelAt(this, EventsHistoryMode.IGNORE_FUTURE_DEACTIVATE);
		if (level > 0) {
			if (message.getType().isRightBorder()) {
				x1 += CommunicationTile.LIVE_DELTA_SIZE * level;
			} else {
				x2 -= CommunicationTile.LIVE_DELTA_SIZE * level;
			}
		}
		final Area area = new Area(x2 - x1, dim.getHeight());
		ug = ug.apply(new UTranslate(x1, 0));
		comp.drawU(ug, area, (Context2D) ug);
	}

	public double getPreferredHeight(StringBounder stringBounder) {
		final Component comp = getComponent(stringBounder);
		final Dimension2D dim = comp.getPreferredDimension(stringBounder);
		return dim.getHeight();
	}

	public void addConstraints(StringBounder stringBounder) {
		final Component comp = getComponent(stringBounder);
		final Dimension2D dim = comp.getPreferredDimension(stringBounder);
		final double width = dim.getWidth();

		final Real point1 = getPoint1(stringBounder);
		final Real point2 = getPoint2(stringBounder);
		if (point1.getCurrentValue() < point2.getCurrentValue()) {
			point2.ensureBiggerThan(point1.addFixed(width));
		} else {
			point1.ensureBiggerThan(point2.addFixed(width));
		}
	}

	public void updateStairs(StringBounder stringBounder, double y) {
		final ArrowComponent comp = (ArrowComponent) getComponent(stringBounder);
		final Dimension2D dim = comp.getPreferredDimension(stringBounder);
		final double arrowY = comp.getStartPoint(stringBounder, dim).getY();

		livingSpace.addStepForLivebox(getEvent(), y + arrowY);

	}

	private Real getPoint1(final StringBounder stringBounder) {
		if (message.getType().isRightBorder()) {
			return livingSpace.getPosC(stringBounder);
		}
		return tileArguments.getOrigin();
	}

	private double getPoint1Value(final StringBounder stringBounder) {
		if (message.getType().isRightBorder()) {
			return livingSpace.getPosC(stringBounder).getCurrentValue();
		}
		return tileArguments.getOrigin().getMinAbsolute().getCurrentValue();
	}

	private Real getPoint2(final StringBounder stringBounder) {
		if (message.getType().isRightBorder()) {
			return tileArguments.getOmega();
		}
		return livingSpace.getPosC(stringBounder);
	}

	public Real getMinX(StringBounder stringBounder) {
		return getPoint1(stringBounder);
	}

	public Real getMaxX(StringBounder stringBounder) {
		return getPoint2(stringBounder);
	}

}