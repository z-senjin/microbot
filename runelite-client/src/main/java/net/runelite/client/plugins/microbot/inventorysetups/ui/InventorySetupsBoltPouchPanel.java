/*
 * Copyright (c) 2019, dillydill123 <https://github.com/dillydill123>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.inventorysetups.ui;

import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetupsItem;
import net.runelite.client.plugins.microbot.inventorysetups.MInventorySetupsPlugin;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetupsSlotID;

import java.util.Arrays;
import java.util.List;
import net.runelite.client.game.ItemManager;

public class InventorySetupsBoltPouchPanel extends InventorySetupsAmmunitionPanel
{
	public static final int BOLT_POUCH_AMOUNT1 = 2469;
	public static final int BOLT_POUCH_AMOUNT2 = 2470;
	public static final int BOLT_POUCH_AMOUNT3 = 2471;
	public static final int BOLT_POUCH_EXTRA_AMMO_AMOUNT = 2472;
	public static final int BOLT_POUCH_BOLT1 = 2473;
	public static final int BOLT_POUCH_BOLT2 = 2474;
	public static final int BOLT_POUCH_BOLT3 = 2475;
	public static final int BOLT_POUCH_EXTRA_AMMO = 2476;

	public static final List<Integer> BOLT_POUCH_AMOUNT_VARBIT_IDS = Arrays.asList(BOLT_POUCH_AMOUNT1, BOLT_POUCH_AMOUNT2, BOLT_POUCH_AMOUNT3, BOLT_POUCH_EXTRA_AMMO_AMOUNT);

	public static final List<Integer> BOLT_POUCH_BOLT_VARBIT_IDS = Arrays.asList(BOLT_POUCH_BOLT1, BOLT_POUCH_BOLT2, BOLT_POUCH_BOLT3, BOLT_POUCH_EXTRA_AMMO);

	InventorySetupsBoltPouchPanel(ItemManager itemManager, MInventorySetupsPlugin plugin)
	{
		super(itemManager, plugin, "Bolt Pouch");
	}

	@Override
	protected InventorySetupsSlotID getSlotId()
	{
		return InventorySetupsSlotID.BOLT_POUCH;
	}

	@Override
	protected int getSlotsCount()
	{
		return 4;
	}

	@Override
	protected List<InventorySetupsItem> getContainer(InventorySetup inventorySetup)
	{
		return inventorySetup.getBoltPouch();
	}

	public void handleBoltPouchHighlighting(final InventorySetup inventorySetup, boolean doesCurrentInventoryHaveBoltPouch)
	{
		if (!inventorySetup.isHighlightDifference() || !plugin.isHighlightingAllowed())
		{
			super.resetSlotColors();
			return;
		}

		if (inventorySetup.getBoltPouch() != null)
		{
			// attempt to highlight if bolt pouch is available
			if (doesCurrentInventoryHaveBoltPouch)
			{
				List<InventorySetupsItem> boltPouchToCheck = plugin.getAmmoHandler().getBoltPouchData();
				super.highlightSlots(boltPouchToCheck, inventorySetup);
			}
			else // if the current inventory doesn't have a bolt pouch but the setup does, highlight the pouch
			{
				super.highlightAllSlots(inventorySetup);
			}
		}
		else
		{
			super.resetSlotColors();
		}
	}
}
