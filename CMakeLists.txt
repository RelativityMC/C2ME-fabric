cmake_minimum_required(VERSION 3.21)
project(C2ME_fabric C)

set(CMAKE_C_STANDARD 11)
set(C_STANDARD 11)

set(CMAKE_C_FLAGS "-Wall -Wextra")
set(CMAKE_C_FLAGS_DEBUG "-g")
set(CMAKE_C_FLAGS_RELEASE "-O3 -mtune=tigerlake -flto")

set(CMAKE_C_COMPILER "clang")

file(GLOB SOURCES
        c2me-natives-opts/src/natives/c/*/*.c
        c2me-natives-opts/src/natives/c/*.c
        c2me-natives-opts/src/natives/include/*/*.c
        c2me-natives-opts/src/natives/include/*.c
        )

add_executable(C2ME_fabric ${SOURCES})

target_link_libraries(C2ME_fabric m)
