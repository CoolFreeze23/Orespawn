package danger.orespawn.block;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockRadish extends CropBlock {
    public BlockRadish(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxAge() {
        return 7;
    }
}
