import io

p = 'src/main/java/danger/orespawn/entity/EasterBunny.java'
s = open(p, encoding='utf-8').read()
cases = open('d4_bunny_cases.txt').read().rstrip('\n')

old_food = '''    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CARROT);
    }'''

new_food = '''    /** orig EasterBunny.java:609-611 - breeding item is the Crystal Apple (no taming; the audit's "carrot taming" claim was wrong). */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /**
     * orig EasterBunny.java:120-128 - 1-in-200 revenge clear, then a 1-in-600
     * roll lays a stack of 1-3 random spawn eggs from the 115-slot table
     * (:130-587). Criminal maps to the port's band_p (see the C7 village-roster
     * audit correction).
     */
    @Override
    protected void customServerAiStep() {
        if (this.random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
        if (this.random.nextInt(600) == 1) {
            layAnEgg(1 + this.random.nextInt(3));
        }
    }

    /** orig EasterBunny.java:130-587 - one nextInt(115) roll picks the egg type; the whole 1-3 count drops as a single stack at plus/minus 0-1 x/z, y+1. */
    private void layAnEgg(int count) {
        net.minecraft.world.item.Item egg = switch (this.random.nextInt(115)) {
%CASES%
            default -> null; // orig :574-579 - rolls 0-4 and 114 lay nothing
        };
        if (egg == null) return;
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                this.level(),
                this.getX() + this.random.nextInt(2) - this.random.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + this.random.nextInt(2) - this.random.nextInt(2),
                new ItemStack(egg, count));
        this.level().addFreshEntity(drop);
    }'''.replace('%CASES%', cases)

assert old_food in s, 'food anchor not found'
s = s.replace(old_food, new_food)
s = s.replace('import danger.orespawn.ModEntities;',
              'import danger.orespawn.ModEntities;\nimport danger.orespawn.ModItems;')
open(p, 'w', encoding='utf-8', newline='').write(s)
print('done')
