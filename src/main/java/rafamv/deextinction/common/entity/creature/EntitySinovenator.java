package rafamv.deextinction.common.entity.creature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafamv.deextinction.DeExtinction;
import rafamv.deextinction.client.renderer.animations.ControlledAnimation;
import rafamv.deextinction.client.renderer.animations.YawChainAnimator;
import rafamv.deextinction.common.database.Creature;
import rafamv.deextinction.common.database.creatures.Sinovenator;
import rafamv.deextinction.common.entity.ai.DEAIAnimatedAttackOnCollideUntilTime;
import rafamv.deextinction.common.entity.ai.DEAIEatDroppedItem;
import rafamv.deextinction.common.entity.ai.DEAIEggNestFinder;
import rafamv.deextinction.common.entity.ai.DEAIEggNestLaying;
import rafamv.deextinction.common.entity.ai.DEAIFollowFood;
import rafamv.deextinction.common.entity.ai.DEAIFollowParent;
import rafamv.deextinction.common.entity.ai.DEAIMating;
import rafamv.deextinction.common.entity.ai.DEAINearestTargetSelectorForPackIfHungry;
import rafamv.deextinction.common.entity.ai.DEAISittingNatural;
import rafamv.deextinction.common.entity.ai.DEAIWander;
import rafamv.deextinction.common.entity.ai.animation.DEAIAnimationAttack;
import rafamv.deextinction.common.entity.ai.animation.DEAnimationList;
import rafamv.deextinction.common.entity.ai.predicate.DETargetSelector;
import rafamv.deextinction.common.entity.ai.predicate.TargetSecondary;
import rafamv.deextinction.common.entity.base.EntityDeExtinctedPack;
import rafamv.deextinction.common.registry.DEDatabaseRegistry;
import rafamv.deextinction.common.registry.DEItemRegistry;

public class EntitySinovenator extends EntityDeExtinctedPack
{
	public YawChainAnimator tailBuffer = new YawChainAnimator(this);

	private ControlledAnimation sittingAnimation = new ControlledAnimation(30);
	private DEAIFollowParent aiFollowParent;
	private DEAIEggNestFinder aiEggNestFinder;
	private DEAIEggNestLaying aiEggNestLaying;
	private DEAIMating aiMating;

	protected static final List<Item> foodList = new ArrayList<Item>();
	static
	{
		EntitySinovenator.foodList.add(Items.beef);
		EntitySinovenator.foodList.add(Items.porkchop);
		EntitySinovenator.foodList.add(Items.chicken);
		EntitySinovenator.foodList.add(Items.mutton);
		EntitySinovenator.foodList.add(Items.fish);
		EntitySinovenator.foodList.add(Items.cooked_beef);
		EntitySinovenator.foodList.add(Items.cooked_porkchop);
		EntitySinovenator.foodList.add(Items.cooked_chicken);
		EntitySinovenator.foodList.add(Items.cooked_mutton);
		EntitySinovenator.foodList.add(Items.cooked_fish);
		EntitySinovenator.foodList.add(Items.bone);
	}

	public EntitySinovenator(World worldIn)
	{
		super(worldIn);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new DEAIAnimatedAttackOnCollideUntilTime(this, 200, 14, false));
		this.tasks.addTask(2, new DEAIAnimationAttack(this, 14, 6));
		this.tasks.addTask(3, new DEAISittingNatural(this, 60, 2000, 500));
		this.tasks.addTask(4, new DEAIFollowFood(this, 30, this.foodList));
		this.tasks.addTask(5, new DEAIEatDroppedItem(this, 40, 0.8D, 16.0D, this.foodList));
		this.tasks.addTask(6, new DEAIWander(this));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 4.0F, 0.025F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(9, new DEAINearestTargetSelectorForPackIfHungry(this, 20, new DETargetSelector(this, new TargetSecondary(EntityChicken.class, 6.0D, 0, 3))));
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.worldObj.isRemote)
		{
			this.tailBuffer.update(4, 30.0F, 3.0F);

			this.sittingAnimation.update();

			if (this.isSitting())
				this.sittingAnimation.runAnimation();
			else
				this.sittingAnimation.stopAnimation();
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entity)
	{
		if (this.animID == 0)
			DeExtinction.sendAnimPacket(this, DEAnimationList.ATTACKING);
		return true;
	}

	@SideOnly(Side.CLIENT)
	public float getSittingProgress(float partialRenderTicks)
	{
		return this.sittingAnimation.getAnimationProgressSin(partialRenderTicks);
	}

	@Override
	public Creature getCreature()
	{
		return DEDatabaseRegistry.LIST_DEEXTINCT_CREATURES.get(Sinovenator.NAME);
	}

	@Override
	public boolean isIndependent()
	{
		return this.getGrowthStage() > 1;
	}

	@Override
	public boolean canBeTamedUponSpawning()
	{
		return false;
	}

	@Override
	public float getEyeHeight()
	{
		return 1.2F * this.height;
	}

	@Override
	protected float getMaxHeight()
	{
		return 1.4F;
	}

	@Override
	protected float getMaxWidth()
	{
		return 0.8F;
	}

	@Override
	protected void updateAIForGrowthStage(byte stage)
	{
		if (this.aiFollowParent == null)
			this.aiFollowParent = new DEAIFollowParent(this, 30);

		if (this.aiEggNestFinder == null)
			this.aiEggNestFinder = new DEAIEggNestFinder(this, 300, 12.0D, 0.8D);

		if (this.aiEggNestLaying == null)
			this.aiEggNestLaying = new DEAIEggNestLaying(this, 1500);

		if (this.aiMating == null)
			this.aiMating = new DEAIMating(this, 18000, 600, 12.0D, 2.5D);

		this.tasks.removeTask(this.aiFollowParent);
		this.tasks.removeTask(this.aiEggNestFinder);
		this.tasks.removeTask(this.aiEggNestLaying);
		this.tasks.removeTask(this.aiMating);

		if (this.isIndependent())
		{
			if (this.isFemale())
			{
				this.tasks.addTask(11, this.aiEggNestLaying);
				this.tasks.addTask(12, this.aiMating);
				this.tasks.addTask(13, this.aiEggNestFinder);
			}
		}
		else
			this.tasks.addTask(10, this.aiFollowParent);
	}

	@Override
	public int getMaxHunger()
	{
		return 425;
	}

	@Override
	public int getTalkInterval()
	{
		return 300;
	}

	@Override
	protected String getLivingSound()
	{
		return DeExtinction.prependModID("sinovenator_changii");
	}

	@Override
	protected float getSoundPitch()
	{
		return this.isChild() ? this.rand.nextFloat() * 0.4F + 1.8F : this.rand.nextFloat() * 0.2F + 1.4F;
	}

	@Override
	protected float getSoundVolume()
	{
		if (this.isIndependent())
			return 0.6F + 0.3F * (this.rand.nextFloat());
		else
			return 0.4F + 0.2F * (this.rand.nextFloat());
	}

	@Override
	public List<Item> getCreatureFoodList()
	{
		return this.foodList;
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting)
	{
		if (this.isIndependent())
		{
			this.dropItemWithOffset(DEItemRegistry.sinovenator_feather, this.rand.nextInt(2), 0.5F * this.height);
			if (this.isBurning())
				this.dropItemWithOffset(DEItemRegistry.sinovenator_cooked, this.rand.nextInt(2), 0.5F * this.height);
			else
				this.dropItemWithOffset(DEItemRegistry.sinovenator_raw, this.rand.nextInt(2), 0.5F * this.height);
		}
	}
}
