#include "../include/common_maths.h"

bool c2me_natives_isInitialized = false;

double c2me_natives_pow_of_two_table[256];

void c2me_natives_init() {
    if (c2me_natives_isInitialized) return;

    for (int i = 0; i < 256; i++) {
        c2me_natives_pow_of_two_table[i] = pow(2.0, i);
    }

    c2me_natives_isInitialized = true;
}
