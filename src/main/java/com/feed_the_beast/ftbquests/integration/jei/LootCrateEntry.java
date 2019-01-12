package com.feed_the_beast.ftbquests.integration.jei;

import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateEntry implements IRecipeWrapper, ITooltipCallback<ItemStack>
{
	public final LootCrate crate;
	public final String name;
	public final ItemStack itemStack;
	public final List<ItemStack> items;

	public LootCrateEntry(LootCrate c)
	{
		crate = c;
		name = crate.table.getDisplayName().getFormattedText();
		itemStack = new ItemStack(FTBQuestsItems.LOOTCRATE);
		itemStack.setTagInfo("type", new NBTTagString(crate.stringID));
		items = new ArrayList<>(c.table.rewards.size());

		ArrayList<WeightedReward> list = new ArrayList<>(c.table.rewards);
		list.sort(null);

		for (WeightedReward reward : list)
		{
			Object object = reward.reward.getJEIFocus();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty())
			{
				items.add(stack.copy());
			}
			else if (reward.reward.getIcon() instanceof ItemIcon)
			{
				stack = ((ItemIcon) reward.reward.getIcon()).getStack().copy();
				stack.setStackDisplayName(reward.reward.getDisplayName().getFormattedText());
				items.add(stack);
			}
			else
			{
				stack = new ItemStack(Items.PAINTING);
				stack.setStackDisplayName(reward.reward.getDisplayName().getFormattedText());
				stack.setTagInfo("icon", new NBTTagString(reward.reward.getIcon().toString()));
				items.add(stack);
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInput(ItemStack.class, itemStack);
		ArrayList<ItemStack> stacks = new ArrayList<>(items);
		stacks.add(itemStack);
		ingredients.setOutputs(ItemStack.class, stacks);
	}

	@Override
	public void onTooltip(int slot, boolean input, ItemStack ingredient, List<String> tooltip)
	{
		if (slot == 0)
		{
			if (crate.table.emptyWeight > 0)
			{
				tooltip.add(TextFormatting.GRAY + I18n.format("jei.ftbquests.lootcrates.no_chance", TextFormatting.GOLD + WeightedReward.chanceString(crate.table.emptyWeight, crate.table.getTotalWeight(true))));
			}

			tooltip.add(TextFormatting.GRAY + I18n.format("jei.ftbquests.lootcrates.dropped_by"));

			int total = ClientQuestFile.INSTANCE.lootCrateNoDrop.passive;

			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null)
				{
					total += table.lootCrate.drops.passive;
				}
			}

			tooltip.add(TextFormatting.GRAY + "- " + I18n.format("ftbquests.loot.entitytype.passive") + ": " + TextFormatting.GOLD + WeightedReward.chanceString(crate.drops.passive, total));

			total = ClientQuestFile.INSTANCE.lootCrateNoDrop.monster;

			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null)
				{
					total += table.lootCrate.drops.monster;
				}
			}

			tooltip.add(TextFormatting.GRAY + "- " + I18n.format("ftbquests.loot.entitytype.monster") + ": " + TextFormatting.GOLD + WeightedReward.chanceString(crate.drops.monster, total));

			total = ClientQuestFile.INSTANCE.lootCrateNoDrop.boss;

			for (RewardTable table : ClientQuestFile.INSTANCE.rewardTables)
			{
				if (table.lootCrate != null)
				{
					total += table.lootCrate.drops.boss;
				}
			}

			tooltip.add(TextFormatting.GRAY + "- " + I18n.format("ftbquests.loot.entitytype.boss") + ": " + TextFormatting.GOLD + WeightedReward.chanceString(crate.drops.boss, total));
		}
		else if (slot > 0 && slot - 1 < items.size())
		{
			tooltip.add(TextFormatting.GRAY + I18n.format("jei.ftbquests.lootcrates.chance", TextFormatting.GOLD + WeightedReward.chanceString(crate.table.rewards.get(slot - 1).weight, crate.table.getTotalWeight(true))));
		}
	}
}