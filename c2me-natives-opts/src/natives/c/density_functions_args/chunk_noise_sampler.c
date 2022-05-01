#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

#pragma pack(push, 4)
typedef struct {
    int horizontalBlockSize, verticalBlockSize;
    int baseX, baseY, baseZ;
    int offsetX, offsetY, offsetZ;
    int minimumY, height;
} chunk_noise_sampler_data;
#pragma pack(pop)

//#include <stdio.h>
//
//int main() {
//    chunk_noise_sampler_data data;
//    printf("horizontalBlockSize: %lu\n", ((size_t) &data.horizontalBlockSize) - ((size_t) &data));
//    printf("verticalBlockSize: %lu\n", ((size_t) &data.verticalBlockSize) - ((size_t) &data));
//    printf("baseX: %lu\n", ((size_t) &data.baseX) - ((size_t) &data));
//    printf("baseY: %lu\n", ((size_t) &data.baseY) - ((size_t) &data));
//    printf("baseZ: %lu\n", ((size_t) &data.baseZ) - ((size_t) &data));
//    printf("offsetX: %lu\n", ((size_t) &data.offsetX) - ((size_t) &data));
//    printf("offsetY: %lu\n", ((size_t) &data.offsetY) - ((size_t) &data));
//    printf("offsetZ: %lu\n", ((size_t) &data.offsetZ) - ((size_t) &data));
//    printf("minimumY: %lu\n", ((size_t) &data.minimumY) - ((size_t) &data));
//    printf("height: %lu\n", ((size_t) &data.height) - ((size_t) &data));
//}

size_t c2me_natives_sizeof_chunk_noise_sampler_data() {
    return sizeof(chunk_noise_sampler_data);
}

noise_pos c2me_natives_dfa_chunk_noise_sampler_get_pos(void *instance, int i) {
    chunk_noise_sampler_data *data = instance;
    int j = c2me_natives_floorMod(i, data->horizontalBlockSize);
    int k = math_floorDiv(i, data->horizontalBlockSize);
    int l = c2me_natives_floorMod(k, data->horizontalBlockSize);
    int m = data->verticalBlockSize - 1 - math_floorDiv(k, data->horizontalBlockSize);

    noise_pos pos;
    pos.x = data->baseX + l;
    pos.y = data->baseY + m;
    pos.z = data->baseZ + j;
    return pos;
}

size_t c2me_natives_dfa_chunk_noise_sampler_get_all_pos(void *instance, noise_pos *array, size_t length) {
    chunk_noise_sampler_data *data = instance;

    size_t index = 0;

    for (int i = data->verticalBlockSize - 1; i >= 0; --i) {
        int blockY = data->baseY + i;

        for (int j = 0; j < data->horizontalBlockSize; ++j) {
            int blockX = data->baseX + j;

            for (int k = 0; k < data->horizontalBlockSize; ++k) {
                int blockZ = data->baseZ + k;

                if (index + 1 >= length) break;

                noise_pos *pos = &array[index++];
                pos->x = blockX;
                pos->y = blockY;
                pos->z = blockZ;
            }
        }
    }

    return index;
}

noise_pos c2me_natives_dfa_chunk_noise_sampler1_get_pos(void *instance, int i) {
    chunk_noise_sampler_data *data = instance;

    noise_pos pos;
    pos.x = data->baseX + data->offsetX;
    pos.y = (i + data->minimumY) * data->verticalBlockSize;
    pos.z = data->baseZ + data->offsetZ;

    return pos;
}

size_t c2me_natives_dfa_chunk_noise_sampler1_get_all_pos(void *instance, noise_pos *array, size_t length) {
    chunk_noise_sampler_data *data = instance;

    int i;
    int len = length;
    for (i = 0; i < len && i < data->height + 1; ++i) {
        array[i].x = data->baseX + data->offsetX;
        array[i].y = (i + data->minimumY) * data->verticalBlockSize;
        array[i].z = data->baseZ + data->offsetZ;
    }

    return i;
}

//density_function_multi_pos_args_data *
//c2me_natives_create_chunk_noise_sampler_data(int horizontalBlockSize, int verticalBlockSize,
//                                             int baseX, int baseY, int baseZ,
//                                             int minimumY, int height) {
//    void *ptr = malloc(sizeof(density_function_multi_pos_args_data) + sizeof(chunk_noise_sampler_data));
//
//    chunk_noise_sampler_data *data = ptr + sizeof(density_function_multi_pos_args_data);
//    data->horizontalBlockSize = horizontalBlockSize;
//    data->verticalBlockSize = verticalBlockSize;
//    data->baseX = baseX;
//    data->baseY = baseY;
//    data->baseZ = baseZ;
//    data->minimumY = minimumY;
//    data->height = height;
//
//    density_function_multi_pos_args_data *dfa = ptr;
//    dfa->instance = data;
//    dfa->get_pos = c2me_natives_dfa_chunk_noise_sampler_get_pos;
//    dfa->get_all_pos = c2me_natives_dfa_chunk_noise_sampler_get_all_pos;
//    return dfa;
//}

density_function_multi_pos_args_data *c2me_natives_create_chunk_noise_sampler_data_empty() {
    void *ptr = malloc(sizeof(density_function_multi_pos_args_data) + sizeof(chunk_noise_sampler_data));
    chunk_noise_sampler_data *data = ptr + sizeof(density_function_multi_pos_args_data);

    density_function_multi_pos_args_data *dfa = ptr;
    dfa->instance = data;
    dfa->get_pos = c2me_natives_dfa_chunk_noise_sampler_get_pos;
    dfa->get_all_pos = c2me_natives_dfa_chunk_noise_sampler_get_all_pos;
    return dfa;
}

density_function_multi_pos_args_data *c2me_natives_create_chunk_noise_sampler1_data_empty() {
    void *ptr = malloc(sizeof(density_function_multi_pos_args_data) + sizeof(chunk_noise_sampler_data));
    chunk_noise_sampler_data *data = ptr + sizeof(density_function_multi_pos_args_data);

    density_function_multi_pos_args_data *dfa = ptr;
    dfa->instance = data;
    dfa->get_pos = c2me_natives_dfa_chunk_noise_sampler1_get_pos;
    dfa->get_all_pos = c2me_natives_dfa_chunk_noise_sampler1_get_all_pos;
    return dfa;
}

