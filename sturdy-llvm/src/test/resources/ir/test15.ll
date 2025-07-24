define i32 @add2(i32 noundef %0) #0 {
  %2 = alloca i32, align 4
  store i32 %0, ptr %2, align 4
  %3 = load i32, ptr %2, align 4
  %4 = add nsw i32 %3, 2
  ret i32 %4
}

define i32 @sub2(i32 noundef %0) #0 {
  %2 = alloca i32, align 4
  store i32 %0, ptr %2, align 4
  %3 = load i32, ptr %2, align 4
  %4 = sub nsw i32 %3, 2
  ret i32 %4
}

define i32 @main() #0 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  store i32 0, ptr %1, align 4
  store i32 10, ptr %2, align 4
  %3 = load i32, ptr %2, align 4
  %4 = icmp sgt i32 %3, 5
  br i1 %4, label %5, label %8

5:                                                ; preds = %0
  %6 = load i32, ptr %2, align 4
  %7 = call i32 @add2(i32 noundef %6)
  store i32 %7, ptr %2, align 4
  br label %11

8:                                                ; preds = %0
  %9 = load i32, ptr %2, align 4
  %10 = call i32 @sub2(i32 noundef %9)
  store i32 %10, ptr %2, align 4
  br label %11

11:                                               ; preds = %8, %5
  %12 = load i32, ptr %2, align 4
  ret i32 %12
}
