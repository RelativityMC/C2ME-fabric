// The following code is from FastNoiseLite, original license below:
// Modified by ebo2022 to include compiler builtins/attributes & use stdint typedefs
//
// -----------------------------------------------------------------------------------------------------
//
// MIT License
//
// Copyright(c) 2023 Jordan Peck (jordan.me2@gmail.com)
// Copyright(c) 2023 Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// .'',;:cldxkO00KKXXNNWWWNNXKOkxdollcc::::::;:::ccllloooolllllllllooollc:,'...        ...........',;cldxkO000Okxdlc::;;;,,;;;::cclllllll
// ..',;:ldxO0KXXNNNNNNNNXXK0kxdolcc::::::;;;,,,,,,;;;;;;;;;;:::cclllllc:;'....       ...........',;:ldxO0KXXXK0Okxdolc::;;;;::cllodddddo
// ...',:loxO0KXNNNNNXXKK0Okxdolc::;::::::::;;;,,'''''.....''',;:clllllc:;,'............''''''''',;:loxO0KXNNNNNXK0Okxdollccccllodxxxxxxd
// ....';:ldkO0KXXXKK00Okxdolcc:;;;;;::cclllcc:;;,''..... ....',;clooddolcc:;;;;,,;;;;;::::;;;;;;:cloxk0KXNWWWWWWNXKK0Okxddoooddxxkkkkkxx
// .....';:ldxkOOOOOkxxdolcc:;;;,,,;;:cllooooolcc:;'...      ..,:codxkkkxddooollloooooooollcc:::::clodkO0KXNWWWWWWNNXK00Okxxxxxxxxkkkkxxx
// . ....';:cloddddo___________,,,,;;:clooddddoolc:,...      ..,:ldx__00OOOkkk___kkkkkkxxdollc::::cclodkO0KXXNNNNNNXXK0OOkxxxxxxxxxxxxddd
// .......',;:cccc:|           |,,,;;:cclooddddoll:;'..     ..';cox|  \KKK000|   |KK00OOkxdocc___;::clldxxkO0KKKKK00Okkxdddddddddddddddoo
// .......'',,,,,''|   ________|',,;;::cclloooooolc:;'......___:ldk|   \KK000|   |XKKK0Okxolc|   |;;::cclodxxkkkkxxdoolllcclllooodddooooo
// ''......''''....|   |  ....'',,,,;;;::cclloooollc:;,''.'|   |oxk|    \OOO0|   |KKK00Oxdoll|___|;;;;;::ccllllllcc::;;,,;;;:cclloooooooo
// ;;,''.......... |   |_____',,;;;____:___cllo________.___|   |___|     \xkk|   |KK_______ool___:::;________;;;_______...'',;;:ccclllloo
// c:;,''......... |         |:::/     '   |lo/        |           |      \dx|   |0/       \d|   |cc/        |'/       \......',,;;:ccllo
// ol:;,'..........|    _____|ll/    __    |o/   ______|____    ___|   |   \o|   |/   ___   \|   |o/   ______|/   ___   \ .......'',;:clo
// dlc;,...........|   |::clooo|    /  |   |x\___   \KXKKK0|   |dol|   |\   \|   |   |   |   |   |d\___   \..|   |  /   /       ....',:cl
// xoc;'...  .....'|   |llodddd|    \__|   |_____\   \KKK0O|   |lc:|   |'\       |   |___|   |   |_____\   \.|   |_/___/...      ...',;:c
// dlc;'... ....',;|   |oddddddo\          |          |Okkx|   |::;|   |..\      |\         /|   |          | \         |...    ....',;:c
// ol:,'.......',:c|___|xxxddollc\_____,___|_________/ddoll|___|,,,|___|...\_____|:\ ______/l|___|_________/...\________|'........',;::cc
// c:;'.......';:codxxkkkkxxolc::;::clodxkOO0OOkkxdollc::;;,,''''',,,,''''''''''',,'''''',;:loxkkOOkxol:;,'''',,;:ccllcc:;,'''''',;::ccll
// ;,'.......',:codxkOO0OOkxdlc:;,,;;:cldxxkkxxdolc:;;,,''.....'',;;:::;;,,,'''''........,;cldkO0KK0Okdoc::;;::cloodddoolc:;;;;;::ccllooo
// .........',;:lodxOO0000Okdoc:,,',,;:clloddoolc:;,''.......'',;:clooollc:;;,,''.......',:ldkOKXNNXX0Oxdolllloddxxxxxxdolccccccllooodddd
// .    .....';:cldxkO0000Okxol:;,''',,;::cccc:;,,'.......'',;:cldxxkkxxdolc:;;,'.......';coxOKXNWWWNXKOkxddddxxkkkkkkxdoollllooddxxxxkkk
//       ....',;:codxkO000OOxdoc:;,''',,,;;;;,''.......',,;:clodkO00000Okxolc::;,,''..',;:ldxOKXNWWWNNK0OkkkkkkkkkkkxxddooooodxxkOOOOO000
//       ....',;;clodxkkOOOkkdolc:;,,,,,,,,'..........,;:clodxkO0KKXKK0Okxdolcc::;;,,,;;:codkO0XXNNNNXKK0OOOOOkkkkxxdoollloodxkO0KKKXXXXX
//
// VERSION: 1.1.1
// https://github.com/Auburn/FastNoiseLite

#pragma once

#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>
#include <float.h>
#include <ext_math.h>

#pragma clang attribute push (__attribute__((always_inline)), apply_to = function)

typedef const enum {
    FNL_NOISE_OPENSIMPLEX2,
    FNL_NOISE_OPENSIMPLEX2S,
    FNL_NOISE_CELLULAR,
    FNL_NOISE_PERLIN,
    FNL_NOISE_VALUE_CUBIC,
    FNL_NOISE_VALUE
} fnl_noise_type;

typedef const enum {
    FNL_ROTATION_NONE,
    FNL_ROTATION_IMPROVE_XY_PLANES,
    FNL_ROTATION_IMPROVE_XZ_PLANES
} fnl_rotation_type_3d;

typedef const enum {
    FNL_FRACTAL_NONE,
    FNL_FRACTAL_FBM,
    FNL_FRACTAL_RIDGED,
    FNL_FRACTAL_PINGPONG,
    FNL_FRACTAL_DOMAIN_WARP_PROGRESSIVE,
    FNL_FRACTAL_DOMAIN_WARP_INDEPENDENT
} fnl_fractal_type;

typedef const enum {
    FNL_CELLULAR_DISTANCE_EUCLIDEAN,
    FNL_CELLULAR_DISTANCE_EUCLIDEANSQ,
    FNL_CELLULAR_DISTANCE_MANHATTAN,
    FNL_CELLULAR_DISTANCE_HYBRID
} fnl_cellular_distance_func;

typedef const enum {
    FNL_CELLULAR_RETURN_TYPE_CELLVALUE,
    FNL_CELLULAR_RETURN_TYPE_DISTANCE,
    FNL_CELLULAR_RETURN_TYPE_DISTANCE2,
    FNL_CELLULAR_RETURN_TYPE_DISTANCE2ADD,
    FNL_CELLULAR_RETURN_TYPE_DISTANCE2SUB,
    FNL_CELLULAR_RETURN_TYPE_DISTANCE2MUL,
    FNL_CELLULAR_RETURN_TYPE_DISTANCE2DIV,
} fnl_cellular_return_type;

typedef const enum {
    FNL_DOMAIN_WARP_OPENSIMPLEX2,
    FNL_DOMAIN_WARP_OPENSIMPLEX2_REDUCED,
    FNL_DOMAIN_WARP_BASICGRID
} fnl_domain_warp_type;

typedef const struct fnl_state {
    int32_t seed;
    float frequency;
    fnl_noise_type noise_type;
    fnl_rotation_type_3d rotation_type_3d;
    fnl_fractal_type fractal_type;
    int32_t octaves;
    float lacunarity;
    float gain;
    float weighted_strength;
    float ping_pong_strength;
    fnl_cellular_distance_func cellular_distance_func;
    fnl_cellular_return_type cellular_return_type;
    float cellular_jitter_mod;
    fnl_domain_warp_type domain_warp_type;
    float domain_warp_amp;
} fnl_state;

__attribute__((aligned(64))) static const float GRADIENTS_3D[] = {
    0, 1, 1, 0,  0,-1, 1, 0,  0, 1,-1, 0,  0,-1,-1, 0,
    1, 0, 1, 0, -1, 0, 1, 0,  1, 0,-1, 0, -1, 0,-1, 0,
    1, 1, 0, 0, -1, 1, 0, 0,  1,-1, 0, 0, -1,-1, 0, 0,
    0, 1, 1, 0,  0,-1, 1, 0,  0, 1,-1, 0,  0,-1,-1, 0,
    1, 0, 1, 0, -1, 0, 1, 0,  1, 0,-1, 0, -1, 0,-1, 0,
    1, 1, 0, 0, -1, 1, 0, 0,  1,-1, 0, 0, -1,-1, 0, 0,
    0, 1, 1, 0,  0,-1, 1, 0,  0, 1,-1, 0,  0,-1,-1, 0,
    1, 0, 1, 0, -1, 0, 1, 0,  1, 0,-1, 0, -1, 0,-1, 0,
    1, 1, 0, 0, -1, 1, 0, 0,  1,-1, 0, 0, -1,-1, 0, 0,
    0, 1, 1, 0,  0,-1, 1, 0,  0, 1,-1, 0,  0,-1,-1, 0,
    1, 0, 1, 0, -1, 0, 1, 0,  1, 0,-1, 0, -1, 0,-1, 0,
    1, 1, 0, 0, -1, 1, 0, 0,  1,-1, 0, 0, -1,-1, 0, 0,
    0, 1, 1, 0,  0,-1, 1, 0,  0, 1,-1, 0,  0,-1,-1, 0,
    1, 0, 1, 0, -1, 0, 1, 0,  1, 0,-1, 0, -1, 0,-1, 0,
    1, 1, 0, 0, -1, 1, 0, 0,  1,-1, 0, 0, -1,-1, 0, 0,
    1, 1, 0, 0,  0,-1, 1, 0, -1, 1, 0, 0,  0,-1,-1, 0
};

__attribute__((aligned(64))) static const float RAND_VECS_3D[] = {
    -0.7292736885f, -0.6618439697f, 0.1735581948f, 0, 0.790292081f, -0.5480887466f, -0.2739291014f, 0, 0.7217578935f, 0.6226212466f, -0.3023380997f, 0, 0.565683137f, -0.8208298145f, -0.0790000257f, 0, 0.760049034f, -0.5555979497f, -0.3370999617f, 0, 0.3713945616f, 0.5011264475f, 0.7816254623f, 0, -0.1277062463f, -0.4254438999f, -0.8959289049f, 0, -0.2881560924f, -0.5815838982f, 0.7607405838f, 0,
    0.5849561111f, -0.662820239f, -0.4674352136f, 0, 0.3307171178f, 0.0391653737f, 0.94291689f, 0, 0.8712121778f, -0.4113374369f, -0.2679381538f, 0, 0.580981015f, 0.7021915846f, 0.4115677815f, 0, 0.503756873f, 0.6330056931f, -0.5878203852f, 0, 0.4493712205f, 0.601390195f, 0.6606022552f, 0, -0.6878403724f, 0.09018890807f, -0.7202371714f, 0, -0.5958956522f, -0.6469350577f, 0.475797649f, 0,
    -0.5127052122f, 0.1946921978f, -0.8361987284f, 0, -0.9911507142f, -0.05410276466f, -0.1212153153f, 0, -0.2149721042f, 0.9720882117f, -0.09397607749f, 0, -0.7518650936f, -0.5428057603f, 0.3742469607f, 0, 0.5237068895f, 0.8516377189f, -0.02107817834f, 0, 0.6333504779f, 0.1926167129f, -0.7495104896f, 0, -0.06788241606f, 0.3998305789f, 0.9140719259f, 0, -0.5538628599f, -0.4729896695f, -0.6852128902f, 0,
    -0.7261455366f, -0.5911990757f, 0.3509933228f, 0, -0.9229274737f, -0.1782808786f, 0.3412049336f, 0, -0.6968815002f, 0.6511274338f, 0.3006480328f, 0, 0.9608044783f, -0.2098363234f, -0.1811724921f, 0, 0.06817146062f, -0.9743405129f, 0.2145069156f, 0, -0.3577285196f, -0.6697087264f, -0.6507845481f, 0, -0.1868621131f, 0.7648617052f, -0.6164974636f, 0, -0.6541697588f, 0.3967914832f, 0.6439087246f, 0,
    0.6993340405f, -0.6164538506f, 0.3618239211f, 0, -0.1546665739f, 0.6291283928f, 0.7617583057f, 0, -0.6841612949f, -0.2580482182f, -0.6821542638f, 0, 0.5383980957f, 0.4258654885f, 0.7271630328f, 0, -0.5026987823f, -0.7939832935f, -0.3418836993f, 0, 0.3202971715f, 0.2834415347f, 0.9039195862f, 0, 0.8683227101f, -0.0003762656404f, -0.4959995258f, 0, 0.791120031f, -0.08511045745f, 0.6057105799f, 0,
    -0.04011016052f, -0.4397248749f, 0.8972364289f, 0, 0.9145119872f, 0.3579346169f, -0.1885487608f, 0, -0.9612039066f, -0.2756484276f, 0.01024666929f, 0, 0.6510361721f, -0.2877799159f, -0.7023778346f, 0, -0.2041786351f, 0.7365237271f, 0.644859585f, 0, -0.7718263711f, 0.3790626912f, 0.5104855816f, 0, -0.3060082741f, -0.7692987727f, 0.5608371729f, 0, 0.454007341f, -0.5024843065f, 0.7357899537f, 0,
    0.4816795475f, 0.6021208291f, -0.6367380315f, 0, 0.6961980369f, -0.3222197429f, 0.641469197f, 0, -0.6532160499f, -0.6781148932f, 0.3368515753f, 0, 0.5089301236f, -0.6154662304f, -0.6018234363f, 0, -0.1635919754f, -0.9133604627f, -0.372840892f, 0, 0.52408019f, -0.8437664109f, 0.1157505864f, 0, 0.5902587356f, 0.4983817807f, -0.6349883666f, 0, 0.5863227872f, 0.494764745f, 0.6414307729f, 0,
    0.6779335087f, 0.2341345225f, 0.6968408593f, 0, 0.7177054546f, -0.6858979348f, 0.120178631f, 0, -0.5328819713f, -0.5205125012f, 0.6671608058f, 0, -0.8654874251f, -0.0700727088f, -0.4960053754f, 0, -0.2861810166f, 0.7952089234f, 0.5345495242f, 0, -0.04849529634f, 0.9810836427f, -0.1874115585f, 0, -0.6358521667f, 0.6058348682f, 0.4781800233f, 0, 0.6254794696f, -0.2861619734f, 0.7258696564f, 0,
    -0.2585259868f, 0.5061949264f, -0.8227581726f, 0, 0.02136306781f, 0.5064016808f, -0.8620330371f, 0, 0.200111773f, 0.8599263484f, 0.4695550591f, 0, 0.4743561372f, 0.6014985084f, -0.6427953014f, 0, 0.6622993731f, -0.5202474575f, -0.5391679918f, 0, 0.08084972818f, -0.6532720452f, 0.7527940996f, 0, -0.6893687501f, 0.0592860349f, 0.7219805347f, 0, -0.1121887082f, -0.9673185067f, 0.2273952515f, 0,
    0.7344116094f, 0.5979668656f, -0.3210532909f, 0, 0.5789393465f, -0.2488849713f, 0.7764570201f, 0, 0.6988182827f, 0.3557169806f, -0.6205791146f, 0, -0.8636845529f, -0.2748771249f, -0.4224826141f, 0, -0.4247027957f, -0.4640880967f, 0.777335046f, 0, 0.5257722489f, -0.8427017621f, 0.1158329937f, 0, 0.9343830603f, 0.316302472f, -0.1639543925f, 0, -0.1016836419f, -0.8057303073f, -0.5834887393f, 0,
    -0.6529238969f, 0.50602126f, -0.5635892736f, 0, -0.2465286165f, -0.9668205684f, -0.06694497494f, 0, -0.9776897119f, -0.2099250524f, -0.007368825344f, 0, 0.7736893337f, 0.5734244712f, 0.2694238123f, 0, -0.6095087895f, 0.4995678998f, 0.6155736747f, 0, 0.5794535482f, 0.7434546771f, 0.3339292269f, 0, -0.8226211154f, 0.08142581855f, 0.5627293636f, 0, -0.510385483f, 0.4703667658f, 0.7199039967f, 0,
    -0.5764971849f, -0.07231656274f, -0.8138926898f, 0, 0.7250628871f, 0.3949971505f, -0.5641463116f, 0, -0.1525424005f, 0.4860840828f, -0.8604958341f, 0, -0.5550976208f, -0.4957820792f, 0.667882296f, 0, -0.1883614327f, 0.9145869398f, 0.357841725f, 0, 0.7625556724f, -0.5414408243f, -0.3540489801f, 0, -0.5870231946f, -0.3226498013f, -0.7424963803f, 0, 0.3051124198f, 0.2262544068f, -0.9250488391f, 0,
    0.6379576059f, 0.577242424f, -0.5097070502f, 0, -0.5966775796f, 0.1454852398f, -0.7891830656f, 0, -0.658330573f, 0.6555487542f, -0.3699414651f, 0, 0.7434892426f, 0.2351084581f, 0.6260573129f, 0, 0.5562114096f, 0.8264360377f, -0.0873632843f, 0, -0.3028940016f, -0.8251527185f, 0.4768419182f, 0, 0.1129343818f, -0.985888439f, -0.1235710781f, 0, 0.5937652891f, -0.5896813806f, 0.5474656618f, 0,
    0.6757964092f, -0.5835758614f, -0.4502648413f, 0, 0.7242302609f, -0.1152719764f, 0.6798550586f, 0, -0.9511914166f, 0.0753623979f, -0.2992580792f, 0, 0.2539470961f, -0.1886339355f, 0.9486454084f, 0, 0.571433621f, -0.1679450851f, -0.8032795685f, 0, -0.06778234979f, 0.3978269256f, 0.9149531629f, 0, 0.6074972649f, 0.733060024f, -0.3058922593f, 0, -0.5435478392f, 0.1675822484f, 0.8224791405f, 0,
    -0.5876678086f, -0.3380045064f, -0.7351186982f, 0, -0.7967562402f, 0.04097822706f, -0.6029098428f, 0, -0.1996350917f, 0.8706294745f, 0.4496111079f, 0, -0.02787660336f, -0.9106232682f, -0.4122962022f, 0, -0.7797625996f, -0.6257634692f, 0.01975775581f, 0, -0.5211232846f, 0.7401644346f, -0.4249554471f, 0, 0.8575424857f, 0.4053272873f, -0.3167501783f, 0, 0.1045223322f, 0.8390195772f, -0.5339674439f, 0,
    0.3501822831f, 0.9242524096f, -0.1520850155f, 0, 0.1987849858f, 0.07647613266f, 0.9770547224f, 0, 0.7845996363f, 0.6066256811f, -0.1280964233f, 0, 0.09006737436f, -0.9750989929f, -0.2026569073f, 0, -0.8274343547f, -0.542299559f, 0.1458203587f, 0, -0.3485797732f, -0.415802277f, 0.840000362f, 0, -0.2471778936f, -0.7304819962f, -0.6366310879f, 0, -0.3700154943f, 0.8577948156f, 0.3567584454f, 0,
    0.5913394901f, -0.548311967f, -0.5913303597f, 0, 0.1204873514f, -0.7626472379f, -0.6354935001f, 0, 0.616959265f, 0.03079647928f, 0.7863922953f, 0, 0.1258156836f, -0.6640829889f, -0.7369967419f, 0, -0.6477565124f, -0.1740147258f, -0.7417077429f, 0, 0.6217889313f, -0.7804430448f, -0.06547655076f, 0, 0.6589943422f, -0.6096987708f, 0.4404473475f, 0, -0.2689837504f, -0.6732403169f, -0.6887635427f, 0,
    -0.3849775103f, 0.5676542638f, 0.7277093879f, 0, 0.5754444408f, 0.8110471154f, -0.1051963504f, 0, 0.9141593684f, 0.3832947817f, 0.131900567f, 0, -0.107925319f, 0.9245493968f, 0.3654593525f, 0, 0.377977089f, 0.3043148782f, 0.8743716458f, 0, -0.2142885215f, -0.8259286236f, 0.5214617324f, 0, 0.5802544474f, 0.4148098596f, -0.7008834116f, 0, -0.1982660881f, 0.8567161266f, -0.4761596756f, 0,
    -0.03381553704f, 0.3773180787f, -0.9254661404f, 0, -0.6867922841f, -0.6656597827f, 0.2919133642f, 0, 0.7731742607f, -0.2875793547f, -0.5652430251f, 0, -0.09655941928f, 0.9193708367f, -0.3813575004f, 0, 0.2715702457f, -0.9577909544f, -0.09426605581f, 0, 0.2451015704f, -0.6917998565f, -0.6792188003f, 0, 0.977700782f, -0.1753855374f, 0.1155036542f, 0, -0.5224739938f, 0.8521606816f, 0.02903615945f, 0,
    -0.7734880599f, -0.5261292347f, 0.3534179531f, 0, -0.7134492443f, -0.269547243f, 0.6467878011f, 0, 0.1644037271f, 0.5105846203f, -0.8439637196f, 0, 0.6494635788f, 0.05585611296f, 0.7583384168f, 0, -0.4711970882f, 0.5017280509f, -0.7254255765f, 0, -0.6335764307f, -0.2381686273f, -0.7361091029f, 0, -0.9021533097f, -0.270947803f, -0.3357181763f, 0, -0.3793711033f, 0.872258117f, 0.3086152025f, 0,
    -0.6855598966f, -0.3250143309f, 0.6514394162f, 0, 0.2900942212f, -0.7799057743f, -0.5546100667f, 0, -0.2098319339f, 0.85037073f, 0.4825351604f, 0, -0.4592603758f, 0.6598504336f, -0.5947077538f, 0, 0.8715945488f, 0.09616365406f, -0.4807031248f, 0, -0.6776666319f, 0.7118504878f, -0.1844907016f, 0, 0.7044377633f, 0.312427597f, 0.637304036f, 0, -0.7052318886f, -0.2401093292f, -0.6670798253f, 0,
    0.081921007f, -0.7207336136f, -0.6883545647f, 0, -0.6993680906f, -0.5875763221f, -0.4069869034f, 0, -0.1281454481f, 0.6419895885f, 0.7559286424f, 0, -0.6337388239f, -0.6785471501f, -0.3714146849f, 0, 0.5565051903f, -0.2168887573f, -0.8020356851f, 0, -0.5791554484f, 0.7244372011f, -0.3738578718f, 0, 0.1175779076f, -0.7096451073f, 0.6946792478f, 0, -0.6134619607f, 0.1323631078f, 0.7785527795f, 0,
    0.6984635305f, -0.02980516237f, -0.715024719f, 0, 0.8318082963f, -0.3930171956f, 0.3919597455f, 0, 0.1469576422f, 0.05541651717f, -0.9875892167f, 0, 0.708868575f, -0.2690503865f, 0.6520101478f, 0, 0.2726053183f, 0.67369766f, -0.68688995f, 0, -0.6591295371f, 0.3035458599f, -0.6880466294f, 0, 0.4815131379f, -0.7528270071f, 0.4487723203f, 0, 0.9430009463f, 0.1675647412f, -0.2875261255f, 0,
    0.434802957f, 0.7695304522f, -0.4677277752f, 0, 0.3931996188f, 0.594473625f, 0.7014236729f, 0, 0.7254336655f, -0.603925654f, 0.3301814672f, 0, 0.7590235227f, -0.6506083235f, 0.02433313207f, 0, -0.8552768592f, -0.3430042733f, 0.3883935666f, 0, -0.6139746835f, 0.6981725247f, 0.3682257648f, 0, -0.7465905486f, -0.5752009504f, 0.3342849376f, 0, 0.5730065677f, 0.810555537f, -0.1210916791f, 0,
    -0.9225877367f, -0.3475211012f, -0.167514036f, 0, -0.7105816789f, -0.4719692027f, -0.5218416899f, 0, -0.08564609717f, 0.3583001386f, 0.929669703f, 0, -0.8279697606f, -0.2043157126f, 0.5222271202f, 0, 0.427944023f, 0.278165994f, 0.8599346446f, 0, 0.5399079671f, -0.7857120652f, -0.3019204161f, 0, 0.5678404253f, -0.5495413974f, -0.6128307303f, 0, -0.9896071041f, 0.1365639107f, -0.04503418428f, 0,
    -0.6154342638f, -0.6440875597f, 0.4543037336f, 0, 0.1074204368f, -0.7946340692f, 0.5975094525f, 0, -0.3595449969f, -0.8885529948f, 0.28495784f, 0, -0.2180405296f, 0.1529888965f, 0.9638738118f, 0, -0.7277432317f, -0.6164050508f, -0.3007234646f, 0, 0.7249729114f, -0.00669719484f, 0.6887448187f, 0, -0.5553659455f, -0.5336586252f, 0.6377908264f, 0, 0.5137558015f, 0.7976208196f, -0.3160000073f, 0,
    -0.3794024848f, 0.9245608561f, -0.03522751494f, 0, 0.8229248658f, 0.2745365933f, -0.4974176556f, 0, -0.5404114394f, 0.6091141441f, 0.5804613989f, 0, 0.8036581901f, -0.2703029469f, 0.5301601931f, 0, 0.6044318879f, 0.6832968393f, 0.4095943388f, 0, 0.06389988817f, 0.9658208605f, -0.2512108074f, 0, 0.1087113286f, 0.7402471173f, -0.6634877936f, 0, -0.713427712f, -0.6926784018f, 0.1059128479f, 0,
    0.6458897819f, -0.5724548511f, -0.5050958653f, 0, -0.6553931414f, 0.7381471625f, 0.159995615f, 0, 0.3910961323f, 0.9188871375f, -0.05186755998f, 0, -0.4879022471f, -0.5904376907f, 0.6429111375f, 0, 0.6014790094f, 0.7707441366f, -0.2101820095f, 0, -0.5677173047f, 0.7511360995f, 0.3368851762f, 0, 0.7858573506f, 0.226674665f, 0.5753666838f, 0, -0.4520345543f, -0.604222686f, -0.6561857263f, 0,
    0.002272116345f, 0.4132844051f, -0.9105991643f, 0, -0.5815751419f, -0.5162925989f, 0.6286591339f, 0, -0.03703704785f, 0.8273785755f, 0.5604221175f, 0, -0.5119692504f, 0.7953543429f, -0.3244980058f, 0, -0.2682417366f, -0.9572290247f, -0.1084387619f, 0, -0.2322482736f, -0.9679131102f, -0.09594243324f, 0, 0.3554328906f, -0.8881505545f, 0.2913006227f, 0, 0.7346520519f, -0.4371373164f, 0.5188422971f, 0,
    0.9985120116f, 0.04659011161f, -0.02833944577f, 0, -0.3727687496f, -0.9082481361f, 0.1900757285f, 0, 0.91737377f, -0.3483642108f, 0.1925298489f, 0, 0.2714911074f, 0.4147529736f, -0.8684886582f, 0, 0.5131763485f, -0.7116334161f, 0.4798207128f, 0, -0.8737353606f, 0.18886992f, -0.4482350644f, 0, 0.8460043821f, -0.3725217914f, 0.3814499973f, 0, 0.8978727456f, -0.1780209141f, -0.4026575304f, 0,
    0.2178065647f, -0.9698322841f, -0.1094789531f, 0, -0.1518031304f, -0.7788918132f, -0.6085091231f, 0, -0.2600384876f, -0.4755398075f, -0.8403819825f, 0, 0.572313509f, -0.7474340931f, -0.3373418503f, 0, -0.7174141009f, 0.1699017182f, -0.6756111411f, 0, -0.684180784f, 0.02145707593f, -0.7289967412f, 0, -0.2007447902f, 0.06555605789f, -0.9774476623f, 0, -0.1148803697f, -0.8044887315f, 0.5827524187f, 0,
    -0.7870349638f, 0.03447489231f, 0.6159443543f, 0, -0.2015596421f, 0.6859872284f, 0.6991389226f, 0, -0.08581082512f, -0.10920836f, -0.9903080513f, 0, 0.5532693395f, 0.7325250401f, -0.396610771f, 0, -0.1842489331f, -0.9777375055f, -0.1004076743f, 0, 0.0775473789f, -0.9111505856f, 0.4047110257f, 0, 0.1399838409f, 0.7601631212f, -0.6344734459f, 0, 0.4484419361f, -0.845289248f, 0.2904925424f, 0
};

static inline __attribute__((const)) float _fnlInterpHermite(const float t) {
    return t * t * (3 - 2 * t);
}

static inline __attribute__((const)) float _fnlInterpQuintic(const float t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
}

static inline __attribute__((const)) float _fnlCubicLerp(const float a, const float b, const float c, const float d, const float t) {
    float p = (d - c) - (a - b);
    return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b;
}

static inline __attribute__((const)) float _fnlPingPong(float t) {
    t -= (int32_t)(t * 0.5f) * 2;
    return t < 1 ? t : 2 - t;
}

static inline __attribute__((const)) float _fnlCalculateFractalBounding(const fnl_state *const state) {
    float gain = fabsf(state->gain);
    float amp = gain;
    float ampFractal = 1.0f;
    for (int32_t i = 1; i < state->octaves; i++) {
        ampFractal += amp;
        amp *= gain;
    }
    return 1.0f / ampFractal;
}

static const int32_t PRIME_X = 501125321;
static const int32_t PRIME_Y = 1136930381;
static const int32_t PRIME_Z = 1720413743;

static inline __attribute__((const)) int32_t _fnlHash3D(const int32_t seed, const int32_t xPrimed, const int32_t yPrimed, const int32_t zPrimed) {
    int32_t hash = seed ^ xPrimed ^ yPrimed ^ zPrimed;
    hash *= 0x27d4eb2d;
    return hash;
}

static inline __attribute__((const)) float _fnlValCoord3D(const int32_t seed, const int32_t xPrimed, const int32_t yPrimed, const int32_t zPrimed) {
    int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed);
    hash *= hash;
    hash ^= hash << 19;
    return hash * (1 / 2147483648.0f);
}

static inline __attribute__((const)) float _fnlGradCoord3D(const int32_t seed, const int32_t xPrimed, const int32_t yPrimed, const int32_t zPrimed, const float xd, const float yd, const float zd) {
    int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed);
    hash ^= hash >> 15;
    hash &= 63 << 2;
    return xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash | 1] + zd * GRADIENTS_3D[hash | 2];
}

static inline void _fnlGradCoordOut3D(const int32_t seed, const int32_t xPrimed, const int32_t yPrimed, const int32_t zPrimed, float *xo, float *yo, float *zo) {
    int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed) & (255 << 2);
    *xo = RAND_VECS_3D[hash];
    *yo = RAND_VECS_3D[hash | 1];
    *zo = RAND_VECS_3D[hash | 2];
}

static inline void _fnlGradCoordDual3D(const int32_t seed, const int32_t xPrimed, const int32_t yPrimed, const int32_t zPrimed, const float xd, const float yd, const float zd, float *xo, float *yo, float *zo){
    int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed);
    int32_t index1 = hash & (63 << 2);
    int32_t index2 = (hash >> 6) & (255 << 2);

    float xg = GRADIENTS_3D[index1];
    float yg = GRADIENTS_3D[index1 | 1];
    float zg = GRADIENTS_3D[index1 | 2];
    float value = xd * xg + yd * yg + zd * zg;

    float xgo = RAND_VECS_3D[index2];
    float ygo = RAND_VECS_3D[index2 | 1];
    float zgo = RAND_VECS_3D[index2 | 2];

    *xo = value * xgo;
    *yo = value * ygo;
    *zo = value * zgo;
}

static inline __attribute__((const)) float _fnlSingleOpenSimplex23D(int32_t seed, const double x, const double y, const double z) {
    // 3D OpenSimplex2 case uses two offset rotated cube grids.

    /*
     * --- Rotation moved to TransformNoiseCoordinate method ---
     * const FNLfloat R3 = (FNLfloat)(2.0 / 3.0);
     * FNLfloat r = (x + y + z) * R3; // Rotation, not skew
     * x = r - x; y = r - y; z = r - z;
     */

    int32_t i = (int32_t)roundf(x);
    int32_t j = (int32_t)roundf(y);
    int32_t k = (int32_t)roundf(z);
    float x0 = (float)(x - i);
    float y0 = (float)(y - j);
    float z0 = (float)(z - k);

    int32_t xNSign = (int32_t)(-1.0f - x0) | 1;
    int32_t yNSign = (int32_t)(-1.0f - y0) | 1;
    int32_t zNSign = (int32_t)(-1.0f - z0) | 1;

    float ax0 = xNSign * -x0;
    float ay0 = yNSign * -y0;
    float az0 = zNSign * -z0;

    i *= PRIME_X;
    j *= PRIME_Y;
    k *= PRIME_Z;

    float value = 0;
    float a = (0.6f - x0 * x0) - (y0 * y0 + z0 * z0);

    for (int l = 0; ; l++) {
        if (a > 0) {
            value += (a * a) * (a * a) * _fnlGradCoord3D(seed, i, j, k, x0, y0, z0);
        }

        float b = a + 1;
        int32_t i1 = i;
        int32_t j1 = j;
        int32_t k1 = k;
        float x1 = x0;
        float y1 = y0;
        float z1 = z0;
        if (ax0 >= ay0 && ax0 >= az0){
            x1 += xNSign;
            b -= xNSign * 2 * x1;
            i1 -= xNSign * PRIME_X;
        } else if (ay0 > ax0 && ay0 >= az0){
            y1 += yNSign;
            b -= yNSign * 2 * y1;
            j1 -= yNSign * PRIME_Y;
        } else {
            z1 += zNSign;
            b -= zNSign * 2 * z1;
            k1 -= zNSign * PRIME_Z;
        }

        if (b > 0) {
            value += (b * b) * (b * b) * _fnlGradCoord3D(seed, i1, j1, k1, x1, y1, z1);
        }

        if (l == 1)
            break;

        ax0 = 0.5f - ax0;
        ay0 = 0.5f - ay0;
        az0 = 0.5f - az0;

        x0 = xNSign * ax0;
        y0 = yNSign * ay0;
        z0 = zNSign * az0;

        a += (0.75f - ax0) - (ay0 + az0);

        i += (xNSign >> 1) & PRIME_X;
        j += (yNSign >> 1) & PRIME_Y;
        k += (zNSign >> 1) & PRIME_Z;

        xNSign = -xNSign;
        yNSign = -yNSign;
        zNSign = -zNSign;

        seed = ~seed;
    }

    return value * 32.69428253173828125f;
}

static inline __attribute__((const)) float _fnlSingleOpenSimplex2S3D(int32_t seed, const double x, const double y, const double z) {
    // 3D OpenSimplex2S case uses two offset rotated cube grids.

    /*
     * --- Rotation moved to TransformNoiseCoordinate method ---
     * const FNLfloat R3 = (FNLfloat)(2.0 / 3.0);
     * FNLfloat r = (x + y + z) * R3; // Rotation, not skew
     * x = r - x; y = r - y; z = r - z;
     */

    int32_t i = (int32_t)floorf(x);
    int32_t j = (int32_t)floorf(y);
    int32_t k = (int32_t)floorf(z);
    float xi = (float)(x - i);
    float yi = (float)(y - j);
    float zi = (float)(z - k);

    i *= PRIME_X;
    j *= PRIME_Y;
    k *= PRIME_Z;
    int32_t seed2 = seed + 1293373;

    int32_t xNMask = (int32_t)(-0.5f - xi);
    int32_t yNMask = (int32_t)(-0.5f - yi);
    int32_t zNMask = (int32_t)(-0.5f - zi);

    float x0 = xi + xNMask;
    float y0 = yi + yNMask;
    float z0 = zi + zNMask;
    float a0 = 0.75f - x0 * x0 - y0 * y0 - z0 * z0;
    float value = (a0 * a0) * (a0 * a0) * _fnlGradCoord3D(seed, i + (xNMask & PRIME_X), j + (yNMask & PRIME_Y), k + (zNMask & PRIME_Z), x0, y0, z0);

    float x1 = xi - 0.5f;
    float y1 = yi - 0.5f;
    float z1 = zi - 0.5f;
    float a1 = 0.75f - x1 * x1 - y1 * y1 - z1 * z1;
    value += (a1 * a1) * (a1 * a1) * _fnlGradCoord3D(seed2, i + PRIME_X, j + PRIME_Y, k + PRIME_Z, x1, y1, z1);

    float xAFlipMask0 = ((xNMask | 1) << 1) * x1;
    float yAFlipMask0 = ((yNMask | 1) << 1) * y1;
    float zAFlipMask0 = ((zNMask | 1) << 1) * z1;
    float xAFlipMask1 = (-2 - (xNMask << 2)) * x1 - 1.0f;
    float yAFlipMask1 = (-2 - (yNMask << 2)) * y1 - 1.0f;
    float zAFlipMask1 = (-2 - (zNMask << 2)) * z1 - 1.0f;

    bool skip5 = false;
    float a2 = xAFlipMask0 + a0;
    if (a2 > 0) {
        float x2 = x0 - (xNMask | 1);
        float y2 = y0;
        float z2 = z0;
        value += (a2 * a2) * (a2 * a2) * _fnlGradCoord3D(seed, i + (~xNMask & PRIME_X), j + (yNMask & PRIME_Y), k + (zNMask & PRIME_Z), x2, y2, z2);
    } else {
        float a3 = yAFlipMask0 + zAFlipMask0 + a0;
        if (a3 > 0) {
            float x3 = x0;
            float y3 = y0 - (yNMask | 1);
            float z3 = z0 - (zNMask | 1);
            value += (a3 * a3) * (a3 * a3) * _fnlGradCoord3D(seed, i + (xNMask & PRIME_X), j + (~yNMask & PRIME_Y), k + (~zNMask & PRIME_Z), x3, y3, z3);
        }
        float a4 = xAFlipMask1 + a1;
        if (a4 > 0) {
            float x4 = (xNMask | 1) + x1;
            float y4 = y1;
            float z4 = z1;
            value += (a4 * a4) * (a4 * a4) * _fnlGradCoord3D(seed2, i + (xNMask & (PRIME_X * 2)), j + PRIME_Y, k + PRIME_Z, x4, y4, z4);
            skip5 = true;
        }
    }

    bool skip9 = false;
    float a6 = yAFlipMask0 + a0;
    if (a6 > 0) {
        float x6 = x0;
        float y6 = y0 - (yNMask | 1);
        float z6 = z0;
        value += (a6 * a6) * (a6 * a6) * _fnlGradCoord3D(seed, i + (xNMask & PRIME_X), j + (~yNMask & PRIME_Y), k + (zNMask & PRIME_Z), x6, y6, z6);
    } else {
        float a7 = xAFlipMask0 + zAFlipMask0 + a0;
        if (a7 > 0) {
            float x7 = x0 - (xNMask | 1);
            float y7 = y0;
            float z7 = z0 - (zNMask | 1);
            value += (a7 * a7) * (a7 * a7) * _fnlGradCoord3D(seed, i + (~xNMask & PRIME_X), j + (yNMask & PRIME_Y), k + (~zNMask & PRIME_Z), x7, y7, z7);
        }

        float a8 = yAFlipMask1 + a1;
        if (a8 > 0) {
            float x8 = x1;
            float y8 = (yNMask | 1) + y1;
            float z8 = z1;
            value += (a8 * a8) * (a8 * a8) * _fnlGradCoord3D(seed2, i + PRIME_X, j + (yNMask & (PRIME_Y << 1)), k + PRIME_Z, x8, y8, z8);
            skip9 = true;
        }
    }

    bool skipD = false;
    float aA = zAFlipMask0 + a0;
    if (aA > 0) {
        float xA = x0;
        float yA = y0;
        float zA = z0 - (zNMask | 1);
        value += (aA * aA) * (aA * aA) * _fnlGradCoord3D(seed, i + (xNMask & PRIME_X), j + (yNMask & PRIME_Y), k + (~zNMask & PRIME_Z), xA, yA, zA);
    } else {
        float aB = xAFlipMask0 + yAFlipMask0 + a0;
        if (aB > 0) {
            float xB = x0 - (xNMask | 1);
            float yB = y0 - (yNMask | 1);
            float zB = z0;
            value += (aB * aB) * (aB * aB) * _fnlGradCoord3D(seed, i + (~xNMask & PRIME_X), j + (~yNMask & PRIME_Y), k + (zNMask & PRIME_Z), xB, yB, zB);
        }

        float aC = zAFlipMask1 + a1;
        if (aC > 0) {
            float xC = x1;
            float yC = y1;
            float zC = (zNMask | 1) + z1;
            value += (aC * aC) * (aC * aC) * _fnlGradCoord3D(seed2, i + PRIME_X, j + PRIME_Y, k + (zNMask & (PRIME_Z << 1)), xC, yC, zC);
            skipD = true;
        }
    }

    if (!skip5) {
        float a5 = yAFlipMask1 + zAFlipMask1 + a1;
        if (a5 > 0) {
            float x5 = x1;
            float y5 = (yNMask | 1) + y1;
            float z5 = (zNMask | 1) + z1;
            value += (a5 * a5) * (a5 * a5) * _fnlGradCoord3D(seed2, i + PRIME_X, j + (yNMask & (PRIME_Y << 1)), k + (zNMask & (PRIME_Z << 1)), x5, y5, z5);
        }
    }

    if (!skip9) {
        float a9 = xAFlipMask1 + zAFlipMask1 + a1;
        if (a9 > 0) {
            float x9 = (xNMask | 1) + x1;
            float y9 = y1;
            float z9 = (zNMask | 1) + z1;
            value += (a9 * a9) * (a9 * a9) * _fnlGradCoord3D(seed2, i + (xNMask & (PRIME_X * 2)), j + PRIME_Y, k + (zNMask & (PRIME_Z << 1)), x9, y9, z9);
        }
    }

    if (!skipD) {
        float aD = xAFlipMask1 + yAFlipMask1 + a1;
        if (aD > 0) {
            float xD = (xNMask | 1) + x1;
            float yD = (yNMask | 1) + y1;
            float zD = z1;
            value += (aD * aD) * (aD * aD) * _fnlGradCoord3D(seed2, i + (xNMask & (PRIME_X << 1)), j + (yNMask & (PRIME_Y << 1)), k + PRIME_Z, xD, yD, zD);
        }
    }

    return value * 9.046026385208288f;
}

static float _fnlSingleCellular3D(const fnl_state *const state, const int32_t seed, const double x, const double y, const double z) {
    int32_t xr = (int32_t)roundf(x);
    int32_t yr = (int32_t)roundf(y);
    int32_t zr = (int32_t)roundf(z);

    float distance0 = FLT_MAX;
    float distance1 = FLT_MAX;
    int32_t closestHash = 0;

    float cellularJitter = 0.39614353f * state->cellular_jitter_mod;

    int32_t xPrimed = (xr - 1) * PRIME_X;
    int32_t yPrimedBase = (yr - 1) * PRIME_Y;
    int32_t zPrimedBase = (zr - 1) * PRIME_Z;

    switch (state->cellular_distance_func) {
        default:
        case FNL_CELLULAR_DISTANCE_EUCLIDEAN:
        case FNL_CELLULAR_DISTANCE_EUCLIDEANSQ:
            for (int32_t xi = xr - 1; xi <= xr + 1; xi++) {
                int32_t yPrimed = yPrimedBase;

                for (int32_t yi = yr - 1; yi <= yr + 1; yi++) {
                    int32_t zPrimed = zPrimedBase;

                    for (int32_t zi = zr - 1; zi <= zr + 1; zi++) {
                        int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed);
                        int32_t idx = hash & (255 << 2);

                        float vecX = (float)(xi - x) + RAND_VECS_3D[idx] * cellularJitter;
                        float vecY = (float)(yi - y) + RAND_VECS_3D[idx | 1] * cellularJitter;
                        float vecZ = (float)(zi - z) + RAND_VECS_3D[idx | 2] * cellularJitter;

                        float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                        distance1 = max(min(distance1, newDistance), distance0);
                        if (newDistance < distance0) {
                            distance0 = newDistance;
                            closestHash = hash;
                        }
                        zPrimed += PRIME_Z;
                    }
                    yPrimed += PRIME_Y;
                }
                xPrimed += PRIME_X;
            }
            break;
    case FNL_CELLULAR_DISTANCE_MANHATTAN:
        for (int32_t xi = xr - 1; xi <= xr + 1; xi++) {
            int32_t yPrimed = yPrimedBase;

            for (int32_t yi = yr - 1; yi <= yr + 1; yi++) {
                int32_t zPrimed = zPrimedBase;

                for (int32_t zi = zr - 1; zi <= zr + 1; zi++) {
                    int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed);
                    int32_t idx = hash & (255 << 2);

                    float vecX = (float)(xi - x) + RAND_VECS_3D[idx] * cellularJitter;
                    float vecY = (float)(yi - y) + RAND_VECS_3D[idx | 1] * cellularJitter;
                    float vecZ = (float)(zi - z) + RAND_VECS_3D[idx | 2] * cellularJitter;

                    float newDistance = fabsf(vecX) + fabsf(vecY) + fabsf(vecZ);

                    distance1 = max(min(distance1, newDistance), distance0);
                    if (newDistance < distance0) {
                        distance0 = newDistance;
                        closestHash = hash;
                    }
                    zPrimed += PRIME_Z;
                }
                yPrimed += PRIME_Y;
            }
            xPrimed += PRIME_X;
        }
        break;
    case FNL_CELLULAR_DISTANCE_HYBRID:
        for (int32_t xi = xr - 1; xi <= xr + 1; xi++) {
            int32_t yPrimed = yPrimedBase;

            for (int32_t yi = yr - 1; yi <= yr + 1; yi++) {
                int32_t zPrimed = zPrimedBase;

                for (int32_t zi = zr - 1; zi <= zr + 1; zi++) {
                    int32_t hash = _fnlHash3D(seed, xPrimed, yPrimed, zPrimed);
                    int32_t idx = hash & (255 << 2);

                    float vecX = (float)(xi - x) + RAND_VECS_3D[idx] * cellularJitter;
                    float vecY = (float)(yi - y) + RAND_VECS_3D[idx | 1] * cellularJitter;
                    float vecZ = (float)(zi - z) + RAND_VECS_3D[idx | 2] * cellularJitter;

                    float newDistance = (fabsf(vecX) + fabsf(vecY) + fabsf(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

                    distance1 = max(min(distance1, newDistance), distance0);
                    if (newDistance < distance0) {
                        distance0 = newDistance;
                        closestHash = hash;
                    }
                    zPrimed += PRIME_Z;
                }
                yPrimed += PRIME_Y;
            }
            xPrimed += PRIME_X;
        }
        break;
    }

    if (state->cellular_distance_func == FNL_CELLULAR_DISTANCE_EUCLIDEAN && state->cellular_return_type >= FNL_CELLULAR_RETURN_TYPE_DISTANCE) {
        distance0 = sqrtf(distance0);
        if (state->cellular_return_type >= FNL_CELLULAR_RETURN_TYPE_DISTANCE2)
            distance1 = sqrtf(distance1);
    }

    switch (state->cellular_return_type) {
        case FNL_CELLULAR_RETURN_TYPE_CELLVALUE:
            return closestHash * (1 / 2147483648.0f);
        case FNL_CELLULAR_RETURN_TYPE_DISTANCE:
            return distance0 - 1;
        case FNL_CELLULAR_RETURN_TYPE_DISTANCE2:
            return distance1 - 1;
        case FNL_CELLULAR_RETURN_TYPE_DISTANCE2ADD:
            return (distance1 + distance0) * 0.5f - 1;
        case FNL_CELLULAR_RETURN_TYPE_DISTANCE2SUB:
            return distance1 - distance0 - 1;
        case FNL_CELLULAR_RETURN_TYPE_DISTANCE2MUL:
            return distance1 * distance0 * 0.5f - 1;
        case FNL_CELLULAR_RETURN_TYPE_DISTANCE2DIV:
            return distance0 / distance1 - 1;
        default:
            return 0;
    }
}

static inline __attribute__((const)) float _fnlSinglePerlin3D(const int32_t seed, const double x, const double y, const double z) {
    int32_t x0 = (int32_t)floorf(x);
    int32_t y0 = (int32_t)floorf(y);
    int32_t z0 = (int32_t)floorf(z);

    float xd0 = (float)(x - x0);
    float yd0 = (float)(y - y0);
    float zd0 = (float)(z - z0);
    float xd1 = xd0 - 1;
    float yd1 = yd0 - 1;
    float zd1 = zd0 - 1;

    float xs = _fnlInterpQuintic(xd0);
    float ys = _fnlInterpQuintic(yd0);
    float zs = _fnlInterpQuintic(zd0);

    x0 *= PRIME_X;
    y0 *= PRIME_Y;
    z0 *= PRIME_Z;
    int32_t x1 = x0 + PRIME_X;
    int32_t y1 = y0 + PRIME_Y;
    int32_t z1 = z0 + PRIME_Z;

    float xf00 = math_lerpf(xs, _fnlGradCoord3D(seed, x0, y0, z0, xd0, yd0, zd0), _fnlGradCoord3D(seed, x1, y0, z0, xd1, yd0, zd0));
    float xf10 = math_lerpf(xs, _fnlGradCoord3D(seed, x0, y1, z0, xd0, yd1, zd0), _fnlGradCoord3D(seed, x1, y1, z0, xd1, yd1, zd0));
    float xf01 = math_lerpf(xs, _fnlGradCoord3D(seed, x0, y0, z1, xd0, yd0, zd1), _fnlGradCoord3D(seed, x1, y0, z1, xd1, yd0, zd1));
    float xf11 = math_lerpf(xs, _fnlGradCoord3D(seed, x0, y1, z1, xd0, yd1, zd1), _fnlGradCoord3D(seed, x1, y1, z1, xd1, yd1, zd1));

    float yf0 = math_lerpf(ys, xf00, xf10);
    float yf1 = math_lerpf(ys, xf01, xf11);

    return math_lerpf(zs, yf0, yf1) * 0.964921414852142333984375f;
}

static inline __attribute__((const)) float _fnlSingleValueCubic3D(const int32_t seed, const double x, const double y, const double z) {
    int32_t x1 = (int32_t)floorf(x);
    int32_t y1 = (int32_t)floorf(y);
    int32_t z1 = (int32_t)floorf(z);

    float xs = x - (float)x1;
    float ys = y - (float)y1;
    float zs = z - (float)z1;

    x1 *= PRIME_X;
    y1 *= PRIME_Y;
    z1 *= PRIME_Z;

    int32_t x0 = x1 - PRIME_X;
    int32_t y0 = y1 - PRIME_Y;
    int32_t z0 = z1 - PRIME_Z;
    int32_t x2 = x1 + PRIME_X;
    int32_t y2 = y1 + PRIME_Y;
    int32_t z2 = z1 + PRIME_Z;
    int32_t x3 = x1 + (int32_t)((long)PRIME_X << 1);
    int32_t y3 = y1 + (int32_t)((long)PRIME_Y << 1);
    int32_t z3 = z1 + (int32_t)((long)PRIME_Z << 1);

    return _fnlCubicLerp(
        _fnlCubicLerp(
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y0, z0), _fnlValCoord3D(seed, x1, y0, z0), _fnlValCoord3D(seed, x2, y0, z0), _fnlValCoord3D(seed, x3, y0, z0), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y1, z0), _fnlValCoord3D(seed, x1, y1, z0), _fnlValCoord3D(seed, x2, y1, z0), _fnlValCoord3D(seed, x3, y1, z0), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y2, z0), _fnlValCoord3D(seed, x1, y2, z0), _fnlValCoord3D(seed, x2, y2, z0), _fnlValCoord3D(seed, x3, y2, z0), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y3, z0), _fnlValCoord3D(seed, x1, y3, z0), _fnlValCoord3D(seed, x2, y3, z0), _fnlValCoord3D(seed, x3, y3, z0), xs),
            ys),
        _fnlCubicLerp(
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y0, z1), _fnlValCoord3D(seed, x1, y0, z1), _fnlValCoord3D(seed, x2, y0, z1), _fnlValCoord3D(seed, x3, y0, z1), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y1, z1), _fnlValCoord3D(seed, x1, y1, z1), _fnlValCoord3D(seed, x2, y1, z1), _fnlValCoord3D(seed, x3, y1, z1), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y2, z1), _fnlValCoord3D(seed, x1, y2, z1), _fnlValCoord3D(seed, x2, y2, z1), _fnlValCoord3D(seed, x3, y2, z1), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y3, z1), _fnlValCoord3D(seed, x1, y3, z1), _fnlValCoord3D(seed, x2, y3, z1), _fnlValCoord3D(seed, x3, y3, z1), xs),
            ys),
        _fnlCubicLerp(
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y0, z2), _fnlValCoord3D(seed, x1, y0, z2), _fnlValCoord3D(seed, x2, y0, z2), _fnlValCoord3D(seed, x3, y0, z2), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y1, z2), _fnlValCoord3D(seed, x1, y1, z2), _fnlValCoord3D(seed, x2, y1, z2), _fnlValCoord3D(seed, x3, y1, z2), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y2, z2), _fnlValCoord3D(seed, x1, y2, z2), _fnlValCoord3D(seed, x2, y2, z2), _fnlValCoord3D(seed, x3, y2, z2), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y3, z2), _fnlValCoord3D(seed, x1, y3, z2), _fnlValCoord3D(seed, x2, y3, z2), _fnlValCoord3D(seed, x3, y3, z2), xs),
            ys),
        _fnlCubicLerp(
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y0, z3), _fnlValCoord3D(seed, x1, y0, z3), _fnlValCoord3D(seed, x2, y0, z3), _fnlValCoord3D(seed, x3, y0, z3), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y1, z3), _fnlValCoord3D(seed, x1, y1, z3), _fnlValCoord3D(seed, x2, y1, z3), _fnlValCoord3D(seed, x3, y1, z3), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y2, z3), _fnlValCoord3D(seed, x1, y2, z3), _fnlValCoord3D(seed, x2, y2, z3), _fnlValCoord3D(seed, x3, y2, z3), xs),
            _fnlCubicLerp(_fnlValCoord3D(seed, x0, y3, z3), _fnlValCoord3D(seed, x1, y3, z3), _fnlValCoord3D(seed, x2, y3, z3), _fnlValCoord3D(seed, x3, y3, z3), xs),
            ys),
        zs) * (1 / (1.5f * 1.5f * 1.5f));
}

static inline __attribute__((const)) float _fnlSingleValue3D(int seed, const double x, const double y, const double z) {
    int x0 = (int32_t)floorf(x);
    int y0 = (int32_t)floorf(y);
    int z0 = (int32_t)floorf(z);

    float xs = _fnlInterpHermite((float)(x - x0));
    float ys = _fnlInterpHermite((float)(y - y0));
    float zs = _fnlInterpHermite((float)(z - z0));

    x0 *= PRIME_X;
    y0 *= PRIME_Y;
    z0 *= PRIME_Z;
    int x1 = x0 + PRIME_X;
    int y1 = y0 + PRIME_Y;
    int z1 = z0 + PRIME_Z;

    float xf00 = math_lerpf(xs, _fnlValCoord3D(seed, x0, y0, z0), _fnlValCoord3D(seed, x1, y0, z0));
    float xf10 = math_lerpf(xs, _fnlValCoord3D(seed, x0, y1, z0), _fnlValCoord3D(seed, x1, y1, z0));
    float xf01 = math_lerpf(xs, _fnlValCoord3D(seed, x0, y0, z1), _fnlValCoord3D(seed, x1, y0, z1));
    float xf11 = math_lerpf(xs, _fnlValCoord3D(seed, x0, y1, z1), _fnlValCoord3D(seed, x1, y1, z1));

    float yf0 = math_lerpf(ys, xf00, xf10);
    float yf1 = math_lerpf(ys, xf01, xf11);

    return math_lerpf(zs, yf0, yf1);
}

static inline float _fnlGenNoiseSingle3D(const fnl_state *const state, const int32_t seed, const double x, const double y, const double z) {
    switch (state->noise_type) {
        case FNL_NOISE_OPENSIMPLEX2:
            return _fnlSingleOpenSimplex23D(seed, x, y, z);
        case FNL_NOISE_OPENSIMPLEX2S:
            return _fnlSingleOpenSimplex2S3D(seed, x, y, z);
        case FNL_NOISE_CELLULAR:
            return _fnlSingleCellular3D(state, seed, x, y, z);
        case FNL_NOISE_PERLIN:
            return _fnlSinglePerlin3D(seed, x, y, z);
        case FNL_NOISE_VALUE_CUBIC:
            return _fnlSingleValueCubic3D(seed, x, y, z);
        case FNL_NOISE_VALUE:
            return _fnlSingleValue3D(seed, x, y, z);
        default:
            return 0;
    }
}

static inline void _fnlTransformNoiseCoordinate3D(const fnl_state *const state, double *x, double *y, double *z) {
    *x *= state->frequency;
    *y *= state->frequency;
    *z *= state->frequency;

    switch (state->rotation_type_3d) {
        case FNL_ROTATION_IMPROVE_XY_PLANES: {
            double xy = *x + *y;
            double s2 = xy * - UNSKEW_FACTOR_2D;
            *z *= INV_SQRT_3;
            *x += s2 - *z;
            *y = *y + s2 - *z;
            *z += xy * INV_SQRT_3;
        }
            break;
        case FNL_ROTATION_IMPROVE_XZ_PLANES: {
            double xz = *x + *z;
            double s2 = xz * - UNSKEW_FACTOR_2D;
            *y *= INV_SQRT_3;
            *x += s2 - *y;
            *z += s2 - *y;
            *y += xz * INV_SQRT_3;
        }
            break;
        default:
            switch (state->noise_type) {
            case FNL_NOISE_OPENSIMPLEX2:
            case FNL_NOISE_OPENSIMPLEX2S: {
                const double R3 = (double)(2.0 / 3.0);
                double r = (*x + *y + *z) * R3; // Rotation, not skew
                *x = r - *x;
                *y = r - *y;
                *z = r - *z;
            }
                    break;
            default:
                    break;
            }
    }
}

static inline __attribute__((const)) float _fnlGenFractalFBM3D(const fnl_state *const state, double x, double y, double z) {
    int32_t seed = state->seed;
    float sum = 0;
    float amp = _fnlCalculateFractalBounding(state);

    for (int32_t i = 0; i < state->octaves; i++){
        float noise = _fnlGenNoiseSingle3D(state, seed++, x, y, z);
        sum += noise * amp;
        amp *= math_lerpf(state->weighted_strength, 1.0f, (noise + 1) * 0.5f);

        x *= state->lacunarity;
        y *= state->lacunarity;
        z *= state->lacunarity;
        amp *= state->gain;
    }

    return sum;
}

static inline __attribute__((const)) float _fnlGenFractalRidged3D(const fnl_state *const state, double x, double y, double z) {
    int32_t seed = state->seed;
    float sum = 0;
    float amp = _fnlCalculateFractalBounding(state);

    for (int32_t i = 0; i < state->octaves; i++) {
        float noise = fabsf(_fnlGenNoiseSingle3D(state, seed++, x, y, z));
        sum += (noise * -2 + 1) * amp;
        amp *= math_lerpf(state->weighted_strength, 1.0f, 1 - noise);

        x *= state->lacunarity;
        y *= state->lacunarity;
        z *= state->lacunarity;
        amp *= state->gain;
    }

    return sum;
}

static inline __attribute__((const)) float _fnlGenFractalPingPong3D(const fnl_state *const state, double x, double y, double z){
    int32_t seed = state->seed;
    float sum = 0;
    float amp = _fnlCalculateFractalBounding(state);

    for (int32_t i = 0; i < state->octaves; i++) {
        float noise = _fnlPingPong((_fnlGenNoiseSingle3D(state, seed++, x, y, z) + 1) * state->ping_pong_strength);
        sum += (noise - 0.5f) * 2 * amp;
        amp *= math_lerpf(state->weighted_strength, 1.0f, noise);

        x *= state->lacunarity;
        y *= state->lacunarity;
        z *= state->lacunarity;
        amp *= state->gain;
    }

    return sum;
}

static inline __attribute__((const)) float fnlGetNoise3D(const fnl_state *const state, double x, double y, double z) {
    _fnlTransformNoiseCoordinate3D(state, &x, &y, &z);

    // Select a noise type
    switch (state->fractal_type){
        default:
            return _fnlGenNoiseSingle3D(state, state->seed, x, y, z);
        case FNL_FRACTAL_FBM:
            return _fnlGenFractalFBM3D(state, x, y, z);
        case FNL_FRACTAL_RIDGED:
            return _fnlGenFractalRidged3D(state, x, y, z);
        case FNL_FRACTAL_PINGPONG:
            return _fnlGenFractalPingPong3D(state, x, y, z);
    }
}

#pragma clang attribute pop
