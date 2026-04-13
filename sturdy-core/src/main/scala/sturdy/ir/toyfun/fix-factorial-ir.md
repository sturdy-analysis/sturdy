





```
def fac(n, acc) :=
  if n = zero
  then acc
	else fac(n - 1, acc * n)

def main(n) := fac(n, 1)


main(N)
enter fac(N, 1)  
=> push fac -> {n = N, acc = 1}
if N = zero: 
  select(N=zero, 1, bot)  {0 -> 1}
if N != zero:
  enter fac(N - 1, 1 * N)
  
  => input widen {n = N, acc = 1} and {n = N - 1, acc = 1 * N} = 
     let f0 = Fix([n, acc], [N - 1, 1 * N], [n, acc], true)
     {n = f0#n, acc=f0#acc}
	if f0#n = zero:
    <= select(f0#n = zero, f0#acc, bot) = 
       let f0' = Fix([n, acc], [N - 1, 1 * N], [n, acc], n != zero)
       f0'#acc   {0 -> bot , 1 -> 1}
  if f0#n != zero:
  	enter fac(f0#n - 1, f0#acc * f0#n)

		=> input widen {n = f0#n, acc=f0#acc} and {n = f0#n - 1, acc=f0#acc * f0#n} =
       let f1 = Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], true)
       {n = f1#n, acc=f1#acc}
    if f1#n = zero:
      <= select(f1#n = zero, f1#acc, bot) = 
         let f1' = Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n != zero)
         f1'#acc  {0 -> bot, 1 -> bot, 2 -> 2, 3 -> 6, 4 -> 24, ...}
    if f1#n != zero:
      enter fac(f1#n - 1, f1#acc * f1#n)
      => input widen {n = f1#n, acc=f1#acc} and {n = f1#n - 1, acc=f1#acc * f0#n} =
         {n = f1#n, acc=f1#acc}
      <= bot (recurrent)
    <= join f1'#acc bot = f1'#acc
    
    => unstable co-recurrent, repeat
       {n = f1#n, acc=f1#acc}
    if f1#n = zero:
      <= select(f1#n = zero, f1#acc, bot) = f1'#acc
    if f1#n != zero:
      enter fac(f1#n - 1, f1#acc * f1#n)
      => input widen {n = f1#n, acc=f1#acc} and {n = f1#n - 1, acc=f1#acc * f0#n} =
         {n = f1#n, acc=f1#acc}
      <= select(f1#n=zero, bot, f1'#acc) = f1'#acc (recurrent)
    <= join f1'#acc f1'#acc = f1'#acc (stable)
    <= select(f0#n = zero, bot, f1'#acc) = f1'#acc
  <= join f0'#acc f1'#acc = f1'#acc {0 -> bot, 1 -> 1, 2 -> 2, 3 -> 6, 4 -> 24, ...}
  <= select(N = zero, bot, f1'#acc)
<= join select(N=zero, 1, bot) select(N = zero, bot, f1'#acc) =
   select(N = zero, 1, f1'#acc) {0 -> 1, 1 -> 1, 2 -> 2, 3 -> 6, 4 -> 24, ...}
   

-- 1
-- fac(N - 1, 1 * N)
  -- 1 * N
  -- fac(N - 1 - 1, 1 * N * (N - 1))
    -- 1 * N * (N - 1)
    -- ...

1 if N = zero
  = 0 -> 1

Fix([n, acc], [N - 1, 1 * N], [n, acc], N - 1 != zero) if N != zero
	= 1 -> 1
Fix([n, acc], [(N - 1) - 1, (1 * N) * (N - 1)], [n, acc], (N - 1) - 1 != zero)  if N - 1 != zero
	= 2 -> 2
--------------------
Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n - 1 != zero) if N != zero
  = 1 -> 1, 2 -> 2, 3 -> 6, n -> n!

if N = zero
then 1
else Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n - 1 != zero)
  = 0 -> 1, 1 -> 1, 2 -> 2, 3 -> 6, n -> n!





Fix([n, acc], [N, 1], [n - 1, acc * n], n != zero)

```



Terminating initial fix nodes

```
def fac(n, acc) :=
  if n = zero
  then acc
	else fac(n - 1, acc * n)

def main(n) := fac(n, 1)


main(N)
enter fac(N, 1)  
=> push fac -> {n = N, acc = 1}
if N = zero: 
  select(N=zero, 1, bot)  {0 -> 1}
if N != zero:
  enter fac(N - 1, 1 * N)
  
  => input widen {n = N, acc = 1} and {n = N - 1, acc = 1 * N} = 
     let f0 = Fix([n, acc], [N - 1, 1 * N], [n, acc], false)
     {n = f0#n, acc=f0#acc}
	if f0#n = zero:
    <= select(f0#n = zero, f0#acc, bot) = 
       let f0' = Fix([n, acc], [N - 1, 1 * N], [n, acc], false || n != zero)
       f0'#acc   {0 -> bot , 1 -> 1}
  if f0#n != zero:
  	enter fac(f0#n - 1, f0#acc * f0#n)

		=> input widen {n = f0#n, acc=f0#acc} and {n = f0#n - 1, acc=f0#acc * f0#n} =
       let f1 = Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], false)
       {n = f1#n, acc=f1#acc}
    if f1#n = zero:
      <= select(f1#n = zero, f1#acc, bot) = 
         let f1' = Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], false || n != zero)
         f1'#acc  {0 -> bot, 1 -> bot, 2 -> 2, 3 -> 6, 4 -> 24, ...}
    if f1#n != zero:
      enter fac(f1#n - 1, f1#acc * f1#n)
      => input widen {n = f1#n, acc=f1#acc} and {n = f1#n - 1, acc=f1#acc * f0#n} =
         {n = f1#n, acc=f1#acc}
      <= bot (recurrent)
    <= join f1'#acc bot = f1'#acc
     / join_cond (f1#n = zero) f1#acc bot
    
    => unstable co-recurrent, repeat
       {n = f1#n, acc=f1#acc}
    if f1#n = zero:
      <= select(f1#n = zero, f1#acc, bot) = f1'#acc
    if f1#n != zero:
      enter fac(f1#n - 1, f1#acc * f1#n)
      => input widen {n = f1#n, acc=f1#acc} and {n = f1#n - 1, acc=f1#acc * f0#n} =
         {n = f1#n, acc=f1#acc}
      <= select(f1#n=zero, bot, f1'#acc) = f1'#acc (recurrent)
    <= join f1'#acc f1'#acc = f1'#acc (stable)
    <= select(f0#n = zero, bot, f1'#acc) = f1'#acc
  <= join f0'#acc f1'#acc = f1'#acc {0 -> bot, 1 -> 1, 2 -> 2, 3 -> 6, 4 -> 24, ...}
  <= select(N = zero, bot, f1'#acc)
<= join select(N=zero, 1, bot) select(N = zero, bot, f1'#acc) =
   select(N = zero, 1, f1'#acc) {0 -> 1, 1 -> 1, 2 -> 2, 3 -> 6, 4 -> 24, ...}
   

-- 1
-- fac(N - 1, 1 * N)
  -- 1 * N
  -- fac(N - 1 - 1, 1 * N * (N - 1))
    -- 1 * N * (N - 1)
    -- ...

1 if N = zero
  = 0 -> 1

Fix([n, acc], [N - 1, 1 * N], [n, acc], N - 1 != zero) if N != zero
	= 1 -> 1
Fix([n, acc], [(N - 1) - 1, (1 * N) * (N - 1)], [n, acc], (N - 1) - 1 != zero)  if N - 1 != zero
	= 2 -> 2
--------------------
Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n - 1 != zero) if N != zero
  = 1 -> 1, 2 -> 2, 3 -> 6, n -> n!

if N = zero
then 1
else Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n - 1 != zero)
  = 0 -> 1, 1 -> 1, 2 -> 2, 3 -> 6, n -> n!





Fix([n, acc], [N, 1], [n - 1, acc * n], n != zero)

```







Upper bound input widening (FAILED!!)

```
def fac(n, acc) :=
  if n = zero
  then acc
	else fac(n - 1, acc * n)

def main(n) := fac(n, 1)


main(N)
enter fac(N, 1)  
=> push fac -> {n = N, acc = 1}
if N = zero: 
  select(N=zero, 1, bot)  {0 -> 1}
if N != zero:
  enter fac(N - 1, 1 * N)
  
  => input widen {n = N, acc = 1} and {n = N - 1, acc = 1 * N} = 
     let f0 = Fix([n, acc], [N - 1, 1 * N], [n, acc], true)
     {n = N-1 lub f0#n, acc=1*N lub f0#acc}
	if N-1 lub f0#n = zero:
    <= select(N-1 lub f0#n = zero, f0#acc, bot) 
            N-1 lub f0#n = zero = N-1 = zero || f0#n = zero
       let f0' = Fix([n, acc], [N - 1, 1 * N], [n, acc], N-1 != zero && n != zero)
       f0'#acc   {0 -> bot , 1 -> 1}
  if f0#n != zero:
  	enter fac(f0#n - 1, f0#acc * f0#n)

		=> input widen {n = f0#n, acc=f0#acc} and {n = f0#n - 1, acc=f0#acc * f0#n} =
       let f1 = Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], true)
       {n = f1#n, acc=f1#acc}
    if f1#n = zero:
      <= select(f1#n = zero, f1#acc, bot) = 
         let f1' = Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n != zero)
         f1'#acc  {0 -> bot, 1 -> bot, 2 -> 2, 3 -> 6, 4 -> 24, ...}
    if f1#n != zero:
      enter fac(f1#n - 1, f1#acc * f1#n)
      => input widen {n = f1#n, acc=f1#acc} and {n = f1#n - 1, acc=f1#acc * f0#n} =
         {n = f1#n, acc=f1#acc}
      <= bot (recurrent)
    <= join f1'#acc bot = f1'#acc
    
    => unstable co-recurrent, repeat
       {n = f1#n, acc=f1#acc}
    if f1#n = zero:
      <= select(f1#n = zero, f1#acc, bot) = f1'#acc
    if f1#n != zero:
      enter fac(f1#n - 1, f1#acc * f1#n)
      => input widen {n = f1#n, acc=f1#acc} and {n = f1#n - 1, acc=f1#acc * f0#n} =
         {n = f1#n, acc=f1#acc}
      <= select(f1#n=zero, bot, f1'#acc) = f1'#acc (recurrent)
    <= join f1'#acc f1'#acc = f1'#acc (stable)
    <= select(f0#n = zero, bot, f1'#acc) = f1'#acc
  <= join f0'#acc f1'#acc = f1'#acc {0 -> bot, 1 -> 1, 2 -> 2, 3 -> 6, 4 -> 24, ...}
  <= select(N = zero, bot, f1'#acc)
<= join select(N=zero, 1, bot) select(N = zero, bot, f1'#acc) =
   select(N = zero, 1, f1'#acc) {0 -> 1, 1 -> 1, 2 -> 2, 3 -> 6, 4 -> 24, ...}
   

-- 1
-- fac(N - 1, 1 * N)
  -- 1 * N
  -- fac(N - 1 - 1, 1 * N * (N - 1))
    -- 1 * N * (N - 1)
    -- ...

1 if N = zero
  = 0 -> 1

Fix([n, acc], [N - 1, 1 * N], [n, acc], N - 1 != zero) if N != zero
	= 1 -> 1
Fix([n, acc], [(N - 1) - 1, (1 * N) * (N - 1)], [n, acc], (N - 1) - 1 != zero)  if N - 1 != zero
	= 2 -> 2
--------------------
Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n - 1 != zero) if N != zero
  = 1 -> 1, 2 -> 2, 3 -> 6, n -> n!

if N = zero
then 1
else Fix([n, acc], [N - 1, 1 * N], [n - 1, acc * n], n - 1 != zero)
  = 0 -> 1, 1 -> 1, 2 -> 2, 3 -> 6, n -> n!





Fix([n, acc], [N, 1], [n - 1, acc * n], n != zero)

```

