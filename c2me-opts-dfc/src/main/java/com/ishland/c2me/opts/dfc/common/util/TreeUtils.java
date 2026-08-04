package com.ishland.c2me.opts.dfc.common.util;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Collection;
import java.util.function.Consumer;

public class TreeUtils {

    private static void enumerate(AstNode node, Consumer<AstNode> consumer) {
        node.transform(astNode -> {
            consumer.accept(astNode);
            return astNode;
        });
    }

    public static Collection<AstNode> findLargestCommonSubtrees(AstNode... roots) {
        if (roots.length < 2) throw new IllegalArgumentException("Cannot find largest common subtrees with less than 2 roots");

        // collect all nodes
        ObjectLinkedOpenHashSet<AstNode> commonNodes = new ObjectLinkedOpenHashSet<>();
        enumerate(roots[0], commonNodes::add);

        // find common nodes
        {
            ObjectOpenHashSet<AstNode> tmp = new ObjectOpenHashSet<>();
            for (int i = 1, rootsLength = roots.length; i < rootsLength; i++) {
                AstNode root = roots[i];
                enumerate(root, tmp::add);
                commonNodes.retainAll(tmp);
                tmp.clear();
            }
        }

        // remove subtrees
        ObjectLinkedOpenHashSet<AstNode> toRemove = new ObjectLinkedOpenHashSet<>();
        for (AstNode node : commonNodes) {
            enumerate(node, node1 -> {
                if (node != node1 && commonNodes.contains(node1)) {
                    toRemove.add(node1);
                }
            });
        }
        commonNodes.removeAll(toRemove);

        return commonNodes;
    }

}
