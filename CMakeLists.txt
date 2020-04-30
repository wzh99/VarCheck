cmake_minimum_required(VERSION 3.0)
project(PAProject)

set(CMAKE_CXX_STANDARD 17)

include_directories(src)

aux_source_directory(src SRC_ROOT)
aux_source_directory(src/analysis SRC_ANALYSIS)
aux_source_directory(src/lang SRC_LANG)
aux_source_directory(src/parse SRC_PARSE)

add_executable(VarChecker ${SRC_ROOT})