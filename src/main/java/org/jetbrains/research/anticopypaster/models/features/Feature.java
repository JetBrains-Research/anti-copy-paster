package org.jetbrains.research.anticopypaster.models.features;

public enum Feature {
    //Meta-Features
    TotalLinesOfCode("TotalLinesOfCode", 0),
    TotalSymbols("TotalSymbols", 1),
    SymbolsPerLine("SymbolsPerLine", 2),
    Area("Area", 3),
    AreaPerLine("AreaPerLine", 4),

    //Historical-Features
    TotalCommitsInFragment("TotalCommitsInFragment", 5),
    TotalAuthorsInFragment("TotalAuthorsInFragment", 6),
    LiveTimeOfFragment("LiveTimeOfFragment", 7),
    LiveTimePerLine("LiveTimePerLine", 8),

    //Coupling-Features
    TotalConnectivity("TotalConnectivity", 9),
    TotalConnectivityPerLine("TotalConnectivityPerLine", 10),
    FieldConnectivity("FieldConnectivity", 11),
    FieldConnectivityPerLine("FieldConnectivityPerLine", 12),
    MethodConnectivity("MethodConnectivity", 13),
    MethodConnectivityPerLine("MethodConnectivityPerLine", 14),

    //Method-Features
    MethodDeclarationLines("MethodDeclarationLines", 15),
    MethodDeclarationSymbols("MethodDeclarationSymbols", 16),
    MethodDeclarationSymbolsPerLine("MethodDeclarationSymbolsPerLine", 17),
    MethodDeclarationArea("MethodDeclarationArea", 18),
    MethodDeclarationAreaPerLine("MethodDeclarationDepthPerLine", 19),

    //Keyword-Features
    KeywordContinueTotalCount("KeywordContinueTotalCount", 20),
    KeywordContinueCountPerLine("KeywordContinueCountPerLine", 21),
    KeywordForTotalCount("KeywordForTotalCount", 22),
    KeywordForCountPerLine("KeywordForCountPerLine", 23),
    KeywordNewTotalCount("KeywordNewTotalCount", 24),
    KeywordNewCountPerLine("KeywordNewCountPerLine", 25),
    KeywordSwitchTotalCount("KeywordSwitchTotalCount", 26),
    KeywordSwitchCountPerLine("KeywordSwitchCountPerLine", 27),
    KeywordAssertTotalCount("KeywordAssertTotalCount", 28),
    KeywordAssertCountPerLine("KeywordAssertCountPerLine", 29),
    KeywordSynchronizedTotalCount("KeywordSynchronizedTotalCount", 30),
    KeywordSynchronizedCountPerLine("KeywordSynchronizedCountPerLine", 31),
    KeywordBooleanTotalCount("KeywordBooleanTotalCount", 32),
    KeywordBooleanCountPerLine("KeywordBooleanCountPerLine", 33),
    KeywordDoTotalCount("KeywordDoTotalCount", 34),
    KeywordDoCountPerLine("KeywordDoCountPerLine", 35),
    KeywordIfTotalCount("KeywordIfTotalCount", 36),
    KeywordIfCountPerLine("KeywordIfCountPerLine", 37),
    KeywordThisTotalCount("KeywordThisTotalCount", 38),
    KeywordThisCountPerLine("KeywordThisCountPerLine", 39),
    KeywordBreakTotalCount("KeywordBreakTotalCount", 40),
    KeywordBreakCountPerLine("KeywordBreakCountPerLine", 41),
    KeywordDoubleTotalCount("KeywordDoubleTotalCount", 42),
    KeywordDoubleCountPerLine("KeywordDoubleCountPerLine", 43),
    KeywordThrowTotalCount("KeywordThrowTotalCount", 44),
    KeywordThrowCountPerLine("KeywordThrowCountPerLine", 45),
    KeywordByteTotalCount("KeywordByteTotalCount", 46),
    KeywordByteCountPerLine("KeywordByteCountPerLine", 47),
    KeywordElseTotalCount("KeywordElseTotalCount", 48),
    KeywordElseCountPerLine("KeywordElseCountPerLine", 49),
    KeywordCaseTotalCount("KeywordCaseTotalCount", 50),
    KeywordCaseCountPerLine("KeywordCaseCountPerLine", 51),
    KeywordInstanceofTotalCount("KeywordInstanceofTotalCount", 52),
    KeywordInstanceofCountPerLine("KeywordInstanceofCountPerLine", 53),
    KeywordReturnTotalCount("KeywordReturnTotalCount", 54),
    KeywordReturnCountPerLine("KeywordReturnCountPerLine", 55),
    KeywordTransientTotalCount("KeywordTransientTotalCount", 56),
    KeywordTransientCountPerLine("KeywordTransientCountPerLine", 57),
    KeywordCatchTotalCount("KeywordCatchTotalCount", 58),
    KeywordCatchCountPerLine("KeywordCatchCountPerLine", 59),
    KeywordIntTotalCount("KeywordIntTotalCount", 60),
    KeywordIntCountPerLine("KeywordIntCountPerLine", 61),
    KeywordShortTotalCount("KeywordShortTotalCount", 62),
    KeywordShortCountPerLine("KeywordShortCountPerLine", 63),
    KeywordTryTotalCount("KeywordTryTotalCount", 64),
    KeywordTryCountPerLine("KeywordTryCountPerLine", 65),
    KeywordCharTotalCount("KeywordCharTotalCount", 66),
    KeywordCharCountPerLine("KeywordCharCountPerLine", 67),
    KeywordFinalTotalCount("KeywordFinalTotalCount", 68),
    KeywordFinalCountPerLine("KeywordFinalCountPerLine", 69),
    KeywordFinallyTotalCount("KeywordFinallyTotalCount", 70),
    KeywordFinallyCountPerLine("KeywordFinallyCountPerLine", 71),
    KeywordLongTotalCount("KeywordLongTotalCount", 72),
    KeywordLongCountPerLine("KeywordLongCountPerLine", 73),
    KeywordStrictfpTotalCount("KeywordStrictfpTotalCount", 74),
    KeywordStrictfpCountPerLine("KeywordStrictfpCountPerLine", 75),
    KeywordFloatTotalCount("KeywordFloatTotalCount", 76),
    KeywordFloatCountPerLine("KeywordFloatCountPerLine", 77),
    KeywordSuperTotalCount("KeywordSuperTotalCount", 78),
    KeywordSuperCountPerLine("KeywordSuperCountPerLine", 79),
    KeywordWhileTotalCount("KeywordWhileTotalCount", 80),
    KeywordWhileCountPerLine("KeywordWhileCountPerLine", 81);

    private final String name;
    private final int id;

    Feature(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static Feature fromId(int id) {
        return Feature.values()[id];
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        if (name.startsWith("Keyword") && name.endsWith("TotalCount")) {
            return "the total count of the " + name.substring(7, 7 + name.length() - "Keyword".length() - "TotalCount".length()) + " keyword";
        }

        if (name.startsWith("Keyword") && name.endsWith("CountPerLine")) {
            return "the average count of the " + name.substring(7, 7 + name.length() - "Keyword".length() - "CountPerLine".length()) + " keyword";
        }

        switch (this) {
            case MethodDeclarationSymbols:
                return "the total size of the enclosing method in symbols";
            case MethodDeclarationSymbolsPerLine:
                return "the per-line-averaged size of the enclosing method in symbols";
            case MethodDeclarationArea:
                return "the total nesting area of the enclosing method";
            case MethodDeclarationAreaPerLine:
                return "the per-line-averaged nesting area of the enclosing method";
            case TotalSymbols:
                return "the total size of the code fragment in symbols";
            case SymbolsPerLine:
                return "the per-line-averaged size of the code fragment in symbols";
            case Area:
                return "the total nested area of the code fragment";
            case AreaPerLine:
                return "the per-line-averaged nested area of the code fragment";
            case TotalLinesOfCode:
                return "the total number of lines of code";
            case TotalConnectivity:
                return "the total coupling with the enclosing class";
            case TotalConnectivityPerLine:
                return "the average coupling with the enclosing class";
            case FieldConnectivity:
                return "the total coupling with the enclosing class by fields";
            case FieldConnectivityPerLine:
                return "the average coupling with the enclosing class by fields";
            case MethodConnectivity:
                return "the total coupling with the enclosing class by methods";
            case MethodConnectivityPerLine:
                return "the average coupling with the enclosing class by methods";
            default:
                return "";
        }
    }

    public int getId() {
        return id;
    }
}

