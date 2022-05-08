
wd <- getwd()
path <- paste(wd, "sturdy-wasm/src/test/resources/sturdy/language/wasm/wasmbench/results", sep="/")

typeCsv <- read.csv2(paste(path, "Type.topmost-calls(1).results.csv", sep="/"), dec = ".")
typeErrorsCsv <- read.csv2(paste(path, "Type.topmost-calls(1).exceptions.csv", sep="/"), dec = ".")
constantCsv <- read.csv2(paste(path, "Constant.topmost-calls(1).results.csv", sep="/"), dec = ".")
constantErrorsCsv <- read.csv2(paste(path, "Constant.topmost-calls(1).exceptions.csv", sep="/"), dec = ".")

typeNewfixCsv <- read.csv2(paste(path, "Type.outermost-calls(1)-newfix.results.csv", sep="/"), dec = ".")
typeNewfixErrorsCsv <- read.csv2(paste(path, "Type.outermost-calls(1)-newfix.exceptions.csv", sep="/"), dec = ".")
constantNewfixCsv <- read.csv2(paste(path, "Constant.outermost-calls(1)-newfix.results.csv", sep="/"), dec = ".")
constantNewfixErrorsCsv <- read.csv2(paste(path, "Constant.outermost-calls(1)-newfix.exceptions.csv", sep="/"), dec = ".")

colors <- c(rgb(237/256, 248/256, 177/256), rgb(127/256, 205/256, 187/256), rgb(44/256, 127/256, 184/256))

typeDuration <- typeCsv$duration / 1000
constantDuration <- constantCsv$duration / 1000

pdf(file = paste(path, "wasmbench-duration.pdf", sep="/"))
boxplot(typeDuration, constantDuration,
        # main = "Multiple boxplots for comparision",
        names = c("running time (types)", "running time (constants)"),
        # las = 2,
        ylim = c(0, 45),
        col = colors
)
dev.off()

typeNewfixDuration <- typeNewfixCsv$duration / 1000
constantNewfixDuration <- constantNewfixCsv$duration / 1000

pdf(file = paste(path, "wasmbench-duration-newfix.pdf", sep="/"))
boxplot(typeNewfixDuration, constantNewfixDuration,
        # main = "Multiple boxplots for comparision",
        names = c("running time (types newfix)", "running time (constants newfix)"),
        # las = 2,
        ylim = c(0, 45),
        col = colors
)
dev.off()

typeDead <- typeCsv$deadInstructionPercent
constantDead <- constantCsv$deadInstructionPercent
constantConstant <- constantCsv$constantInstructionPercent

pdf(file = paste(path, "wasmbench-results.pdf", sep="/"))
b <- boxplot(typeDead, constantDead, constantConstant,
        # main = "Multiple boxplots for comparision",
        names = c("dead instr. (types)", "dead instr. (constants)", "constant instr."),
        # las = 2,
        # ylim = c(0, 45000),
        col = colors
)
dev.off()

typeNewfixDead <- typeNewfixCsv$deadInstructionPercent
constantNewfixDead <- constantNewfixCsv$deadInstructionPercent
constantNewfixConstant <- constantNewfixCsv$constantInstructionPercent

pdf(file = paste(path, "wasmbench-results-newfix.pdf", sep="/"))
b <- boxplot(typeNewfixDead, constantNewfixDead, constantNewfixConstant,
             # main = "Multiple boxplots for comparision",
             names = c("dead (types newfix)", "dead (constants newfix)", "constant newfix"),
             # las = 2,
             # ylim = c(0, 45000),
             col = colors
)
dev.off()


typeSuccessRuns <- length(typeCsv$duration)
typeErrorMsgs <- typeErrorsCsv$exceptionMsg
typeTimeouts <- length(typeErrorMsgs[typeErrorMsgs=="java.lang.InterruptedException"])
typeInvalidImport <- length(typeErrorMsgs[grepl("No module with name", typeErrorMsgs)])
typeInvalidMemory <- length(typeErrorMsgs[grepl("swam.validation.ValidationException: memory size may not exceed 1024 pages", typeErrorMsgs)])
typeInvalidHostFunction <- length(typeErrorMsgs[grepl("host", typeErrorMsgs)])
typeOtherErrors <- length(typeErrorMsgs) - typeTimeouts - typeInvalidImport - typeInvalidMemory - typeInvalidHostFunction

constantSuccessRuns <- length(constantCsv$duration)
constantErrorMsgs <- constantErrorsCsv$exceptionMsg
constantTimeouts <- length(constantErrorMsgs[constantErrorMsgs=="java.lang.InterruptedException"])
constantInvalidImport <- length(constantErrorMsgs[grepl("No module with name", constantErrorMsgs)])
constantInvalidMemory <- length(constantErrorMsgs[grepl("swam.validation.ValidationException: memory size may not exceed 1024 pages", constantErrorMsgs)])
constantInvalidHostFunction <- length(constantErrorMsgs[grepl("host", constantErrorMsgs)])
constantOtherErrors <- length(constantErrorMsgs) - constantTimeouts - constantInvalidImport - constantInvalidMemory - constantInvalidHostFunction
