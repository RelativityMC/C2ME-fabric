#pragma once

#define TARGET_IMPL_ARCH_(suffix, func_prefix, func_ret, func_call) \
  func_ret func_prefix##_##suffix func_call

#define TARGET_IMPL_ARCH(suffix, func_prefix, func_ret, func_call) \
  TARGET_IMPL_ARCH_(suffix, func_prefix, func_ret, func_call)

#ifdef __x86_64__

#define MACRO_VALUE_OF(x) x
#define STRINGIZE(x) #x
#define STRINGIZE_VALUE_OF(x) STRINGIZE(x)

#ifndef UARCH_SUFFIX
#define UARCH_SUFFIX sse2
#endif

#define TARGET_IMPL(func_prefix, func_ret, func_call) \
  TARGET_IMPL_ARCH(UARCH_SUFFIX, func_prefix, func_ret, func_call)

#else

#define TARGET_IMPL(func_prefix, func_ret, func_call) \
  __attribute__((pure)) TARGET_IMPL_ARCH(generic, func_prefix, func_ret)

#endif

