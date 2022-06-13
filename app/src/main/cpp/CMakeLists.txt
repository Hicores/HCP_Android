cmake_minimum_required(VERSION 3.18.1)
project(native_lib)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections")

find_library(
        xposeddetector
        ANDROID_LIBS
        log)
find_package(xposeddetector REQUIRED CONFIG)
add_library(
        native_lib SHARED
        app.cpp
)
target_link_libraries(native_lib xposeddetector::xposed_detector)

