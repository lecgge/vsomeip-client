cmake_minimum_required(VERSION 3.18.1)

project("vsomeip")


include_directories(include)


find_library(log-lib log)

add_library(ClientJNI SHARED ClientJNI.cpp)
target_link_libraries(ClientJNI ${log-lib} vsomeip3 vsomeip3-cfg vsomeip3-e2e vsomeip3-sd)