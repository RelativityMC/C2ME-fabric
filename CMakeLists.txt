cmake_minimum_required(VERSION 3.21)
project(C2ME_fabric C)

set(CMAKE_C_STANDARD 11)
set(C_STANDARD 11)

set(CMAKE_C_FLAGS "-Wall -Wextra -march=x86-64")
set(CMAKE_C_FLAGS_DEBUG "-g")
set(CMAKE_C_FLAGS_RELEASE "-O3 -mtune=tigerlake")

add_executable(C2ME_fabric
        c2me-natives-opts/src/natives/c/perlin_noise_sampler.c c2me-natives-opts/src/natives/includes/common_maths.h c2me-natives-opts/src/natives/c/simplex_noise_sampler.c c2me-natives-opts/src/natives/c/init.c)

target_link_libraries(C2ME_fabric m)
