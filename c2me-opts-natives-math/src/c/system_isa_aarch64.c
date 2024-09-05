#ifdef __aarch64__

#include <stdint.h>

int32_t c2me_natives_get_system_isa(_Bool allowAVX512) {
    return 0;
}

#endif

typedef int make_iso_compiler_happy;
