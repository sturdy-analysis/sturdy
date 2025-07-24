define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  %3 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 7, ptr %2, align 4
  %4 = load i32, ptr %2, align 4
  %5 = icmp sgt i32 %4, 0
  br i1 %5, label %6, label %12

6:                                                ; preds = %0
  %7 = load i32, ptr %2, align 4
  %8 = icmp slt i32 %7, 10
  br i1 %8, label %9, label %10

9:                                                ; preds = %6
  store i32 1, ptr %3, align 4
  br label %11

10:                                               ; preds = %6
  store i32 2, ptr %3, align 4
  br label %11

11:                                               ; preds = %10, %9
  br label %12

12:                                               ; preds = %11, %0
  %13 = load i32, ptr %3, align 4
  ret i32 %13
}
