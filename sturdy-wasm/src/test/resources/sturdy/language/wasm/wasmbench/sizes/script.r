
wd <- getwd()
path <- paste(wd, "sturdy-wasm/src/test/resources/sturdy/language/wasm/wasmbench/sizes", sep="/")


typeCsv <- read.csv(paste(path, "type_results_size.csv", sep="/"), dec = ".")
constantCsv <- read.csv(paste(path, "constant_results_size.csv", sep="/"), dec = ".")
taintCsv <- read.csv(paste(path, "taint_results_size.csv", sep="/"), dec = ".")

constantResultCsv <- read.csv2(paste(path, "../results/ConstantAnalysis(config=innermost(StackedStates(true))_calls(1),scope=MostGeneralClient).results.csv", sep="/"), dec = ".")

typeCol <- rgb(127/256, 205/256, 187/256)
constantCol <- rgb(44/256, 127/256, 184/256)
taintCol <- rgb(237/256, 248/256, 177/256)

durationColors <- c(rgb(127/256, 205/256, 187/256), rgb(44/256, 127/256, 184/256), rgb(237/256, 248/256, 177/256))
colors <- c(rgb(127/256, 205/256, 187/256), rgb(44/256, 127/256, 184/256), rgb(44/256, 127/256, 184/256), rgb(237/256, 248/256, 177/256))

typeSize <- typeCsv$size_bytes
constantSize <- constantCsv$size_bytes
taintSize <- taintCsv$size_bytes

constantInstructions <- constantResultCsv$allInstructions

options(scipen=5)
pdf(file = paste(path, "wasmbench-mgc-sizes.pdf", sep="/"), width = 6, height = 3)
boxplot(constantSize / 1024,
        # main = "Multiple boxplots for comparision",
        xlab = "Size in KB",
        # las = 2,
        # ylim = c(0, 3800000),
        log = "x",
        horizontal=TRUE,
        col = rgb(127/256, 205/256, 187/256)
        # height = 1,
        # width = 1
)
# means <- c(mean(typeSize), mean(constantSize), mean(taintSize))
# points(means, pch = 'x', col = "red" )
# text(means, labels = paste(round(means), "B"), col = "red", pos = 4, offset = 2.5)
dev.off()

pdf(file = paste(path, "wasmbench-mgc-instcount.pdf", sep="/"), width = 6, height = 3)
boxplot(constantInstructions,
        # main = "Multiple boxplots for comparision",
        xlab = "Instruction count",
        # las = 2,
        # ylim = c(0, 3800000),
        log = "x",
        horizontal=TRUE,
        col = rgb(127/256, 205/256, 187/256)
        # height = 1,
        # width = 1
)
# means <- c(mean(typeSize), mean(constantSize), mean(taintSize))
# points(means, pch = 'x', col = "red" )
# text(means, labels = paste(round(means), "B"), col = "red", pos = 4, offset = 2.5)
dev.off()


# typeDead <- typeCsv$deadInstructionPercent
# typeDeadMedian <- median(typeDead)
# constantDead <- constantCsv$deadInstructionPercent
# constantDeadMedian <- median(constantDead)
# constantConstant <- constantCsv$constantInstructionPercent
# constantConstantMedian <- median(constantConstant)
# safeMem <- 100 - taintCsv$taintedAccessesPercent
# safeMemMedian <- median(safeMem)

# pdf(file = paste(path, "wasmbench-mgc-results.pdf", sep="/"))
# b <- boxplot(typeDead, constantDead, constantConstant, safeMem,
#         # main = "Multiple boxplots for comparision",
#         names = c("dead code\n(type values)", "dead code\n(constant values)", "constant\ninstructions", "safe memory\ninstructions"),
#         # las = 2,
#         # ylim = c(0, 45000),
#         ylab = "Percentage (%) of instructions",
# #        pars=list(outcol=c(typeCol, typeCol, constantCol, taintCol)),
#              # xlab = "Analysis",
#         col = c(typeCol, typeCol, constantCol, taintCol)
# )
# means <- c(mean(typeDead), mean(constantDead), mean(constantConstant), mean(safeMem))
# points(means, pch = 'x', col = "red" )
# text(means, labels = paste(round(means), '%'), col = "red", pos = 4, offset = 2.5)
# dev.off()

# typeSuccessRuns <- length(typeCsv$duration)
# typeSuccessRuns10s <- length(typeSize[typeSize <= 10]) / typeSuccessRuns
# typeErrorMsgs <- typeErrorsCsv$exceptionMsg
# typeTimeouts <- length(typeErrorMsgs[typeErrorMsgs=="java.lang.InterruptedException"])
# typeInvalidImport <- length(typeErrorMsgs[grepl("No module with name", typeErrorMsgs)])
# typeInvalidMemory <- length(typeErrorMsgs[grepl("swam.validation.ValidationException: memory size may not exceed 1024 pages", typeErrorMsgs)])
# typeInvalidHostFunction <- length(typeErrorMsgs[grepl("host", typeErrorMsgs)])
# typeParseError <- length(typeErrorMsgs[grepl("WasmParseError", typeErrorMsgs)])
# typeOtherErrors <- length(typeErrorMsgs) - typeTimeouts - typeInvalidImport - typeInvalidMemory - typeInvalidHostFunction - typeParseError

# constantSuccessRuns <- length(constantCsv$duration)
# constantSuccessRuns10s <- length(constantSize[constantSize <= 10]) / constantSuccessRuns
# constantErrorMsgs <- constantErrorsCsv$exceptionMsg
# constantTimeouts <- length(constantErrorMsgs[constantErrorMsgs=="java.lang.InterruptedException"])
# constantInvalidImport <- length(constantErrorMsgs[grepl("No module with name", constantErrorMsgs)])
# constantInvalidMemory <- length(constantErrorMsgs[grepl("swam.validation.ValidationException: memory size may not exceed 1024 pages", constantErrorMsgs)])
# constantInvalidHostFunction <- length(constantErrorMsgs[grepl("host", constantErrorMsgs)])
# constantParseError <- length(constantErrorMsgs[grepl("WasmParseError", constantErrorMsgs)])
# constantOtherErrors <- length(constantErrorMsgs) - constantTimeouts - constantInvalidImport - constantInvalidMemory - constantInvalidHostFunction - constantParseError

# taintSuccessRuns <- length(taintCsv$duration)
# taintSuccessRuns10s <- length(taintSize[taintSize <= 10]) / taintSuccessRuns
# taintErrorMsgs <- taintErrorsCsv$exceptionMsg
# taintTimeouts <- length(taintErrorMsgs[constantErrorMsgs=="java.lang.InterruptedException"])
# taintInvalidImport <- length(taintErrorMsgs[grepl("No module with name", constantErrorMsgs)])
# taintInvalidMemory <- length(taintErrorMsgs[grepl("swam.validation.ValidationException: memory size may not exceed 1024 pages", constantErrorMsgs)])
# taintInvalidHostFunction <- length(taintErrorMsgs[grepl("host", constantErrorMsgs)])
# taintParseError <- length(taintErrorMsgs[grepl("WasmParseError", constantErrorMsgs)])
# taintOtherErrors <- length(taintErrorMsgs) - constantTimeouts - constantInvalidImport - constantInvalidMemory - constantInvalidHostFunction - constantParseError


# memsafeInstPercent <- 100 - taintCsv$taintedAccessesPercent
# memsafeBinaries <- length(which(memsafeInstPercent == 100)) / length(memsafeInstPercent)
# memsafeInstMean <- mean(memsafeInstPercent)

# pdf(file = paste(path, "wasmbench-mgc-memsafe.pdf", sep="/"))
# hist(memsafeYesNo)
# dev.off()
