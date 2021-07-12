package com.ishland.c2me.compatibility.mixin.terra;

import com.dfsek.terra.api.TerraPlugin;
import com.dfsek.terra.api.structures.parser.Parser;
import com.dfsek.terra.api.structures.parser.exceptions.ParseException;
import com.dfsek.terra.api.structures.parser.lang.Block;
import com.dfsek.terra.api.structures.script.StructureScript;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureScript.class)
public class MixinStructureScript {

    @Shadow @Final private TerraPlugin main;
    private ThreadLocal<Block> blockThreadLocal = new ThreadLocal<>();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/dfsek/terra/api/structures/parser/Parser;parse()Lcom/dfsek/terra/api/structures/parser/lang/Block;"))
    private Block redirectParse(Parser parser) throws ParseException {
        final Block parsedBlock = parser.parse();
        blockThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                long startTime = System.nanoTime();
                final Block parse = parser.parse();
                if (main != null) main.getDebugLogger().info(String.format("[C2ME Compatibility Module] Compiled script \"%s\" for thread \"%s\" after %.2fms", parser.getID(), Thread.currentThread().getName(), (System.nanoTime() - startTime) / 1_000_000.0));
                return parse;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        return parsedBlock;
    }

    @Redirect(method = "applyBlock", at = @At(value = "FIELD", target = "Lcom/dfsek/terra/api/structures/script/StructureScript;block:Lcom/dfsek/terra/api/structures/parser/lang/Block;"), remap = false)
    private Block redirectBlockUsage(StructureScript unused) {
        return blockThreadLocal.get();
    }

}
