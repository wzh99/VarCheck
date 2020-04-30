##  程序分析大作业 LLVM 版要求

1. 针对给定的 LLVM IR，构建控制流图，程序的控制跳转通过 `br`, `ret` 实现。

2. 在 LLVM 中，局部变量有两种存储方式：虚拟寄存器和栈。

    虚拟寄存器（以下简称寄存器）中直接存储变量的值，并且仅限于基本类型和指针类型。对于指针，不考虑 `getelementptr` 等涉及指针的运算。对于存储在寄存器中的变量 `%a`，其访问模式包括：

    * 定义：`%a = ...`
    * 使用：`%b = ..., %a, ...` （包括算数运算、逻辑运算、函数调用等）

    栈上的变量通过存储在寄存器中的指针来访问。这里只考虑在栈上分配基本类型变量，不考虑指针类型、聚合类型（数组、结构体等）。同时，不考虑将栈上变量的地址传递到其他函数的操作。对于存储在栈上的变量，假设指向其的指针为 `%p`，其访问模式包括：

    * 分配栈空间：`%p = alloca ...`
    * 写入：`store ..., %p, ...`
    * 读取：`%a = load ..., %p, ...`

    对每一条指令，确定其相关的变量和类型。

3. 检查是否存在不合理使用的变量并输出。对于寄存器变量，其应该先定义后使用。如果未定义就使用，则认为是不合理的。对于栈上变量，在分配空间后，应该先写入再读取。如果未写入就读取，则认为是不合理的。

注：LLVM 版要求中不考虑指针运算，以及不考虑在栈上分配指针类型和聚合类型变量，是为了避免别名分析。不考虑传递栈上变量的地址是为了避免过程间分析。别名分析和过程间分析在原 Jimple 版要求中均没有涉及。



以下给出一个 LLVM IR 的示例，其包含对于栈上变量未写入就读取的情况。

首先使用 C 语言编写程序 `uninit.c`，注意到对于局部变量 `j` ，其在 `if` 的 `else` 分支内未被定义，但在随后的 `2 * j` 表达式中被使用。

```c
// uninit.c
int two() { return 2; }

int main() {
    int j;
    if (two() != 2) {
        j = 1;
    }
    int a = 2 * j;
}
```

使用 Clang 输出 LLVM IR：

```
> clang -S -emit-llvm -fno-discard-value-names uninit.c
```

得到如下代码，和函数无关的部分已经删去。

```assembly
define i32 @two() #0 {
entry:
  ret i32 2
}

define i32 @main() #0 {
entry:
  %retval = alloca i32, align 4
  %j = alloca i32, align 4
  %a = alloca i32, align 4
  store i32 0, i32* %retval, align 4
  %call = call i32 @two()
  %cmp = icmp ne i32 %call, 2
  br i1 %cmp, label %if.then, label %if.end

if.then:                                          ; preds = %entry
  store i32 1, i32* %j, align 4
  br label %if.end

if.end:                                           ; preds = %if.then, %entry
  %0 = load i32, i32* %j, align 4
  %mul = mul nsw i32 2, %0
  store i32 %mul, i32* %a, align 4
  %1 = load i32, i32* %retval, align 4
  ret i32 %1
}
```

可以看到，对于指向栈上变量的指针 `%j` ，在 `entry -> if.end` 这条路径上没有关于它的 `store` 指令，但在 `if.end`  中有关于它的 `load` 指令，属于未写入就读取，是不合理的使用。

