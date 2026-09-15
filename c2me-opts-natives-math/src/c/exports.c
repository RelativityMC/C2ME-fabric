#include <ext_math.h>
#include <FastNoiseLite.h>
#include <target_macros.h>

TARGET_IMPL(c2me_natives_end_islands_sample, float, (const aligned_uint32_ptr simplex_permutations, const int32_t x, const int32_t z) {
    return math_end_islands_sample(simplex_permutations, x, z);
})

TARGET_IMPL(c2me_natives_biome_access_sample, uint32_t, (const int64_t theSeed, const int32_t x, const int32_t y, const int32_t z) {
    return math_biome_access_sample(theSeed, x, y, z);
})

TARGET_IMPL(c2me_natives_aquifer_refreshDistPosIdx, void, (const uint16_t *restrict const packedBlockPositions,
                                                           uint32_t *restrict const res,
                                                           const aquifer_data_t *restrict const aquiferData,
                                                           const int32_t x, const int32_t y, const int32_t z) {
    math_aquifer_refreshDistPosIdx(packedBlockPositions, res, aquiferData, x, y, z);
})

//TARGET_IMPL(c2me_natives_biome_search_tree_calc, uint32_t, (const biome_search_tree_node_t * restrict const nodes,
//                                                             const uint16_t * restrict const target,
//                                                             const uint32_t nodes_c, const uint32_t tree_depth) {
//    return math_biome_search_tree_calc(nodes, target, nodes_c, tree_depth);
//})

TARGET_IMPL(c2me_natives_biome_search_tree_calc_args, uint32_t, (const biome_search_tree_node_t * restrict const nodes,
                                                                 const uint32_t nodes_c, const uint32_t tree_depth,
                                                                 int16_t p0, int16_t p1, int16_t p2, int16_t p3,
                                                                 int16_t p4, int16_t p5, int16_t p6) {
    return math_biome_search_tree_calc_args(nodes, nodes_c, tree_depth, p0, p1, p2, p3, p4, p5, p6);
})

// TARGET_IMPL(c2me_natives_fnlGetNoise3D, float, (const fnl_state *const state, double x, double y, double z) {
//     return fnlGetNoise3D(state, x, y, z);
// })
