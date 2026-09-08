package com.parcialimplementacion.parcialenanosvscamellos.result.util;

import com.parcialimplementacion.parcialenanosvscamellos.result.entity.ResultStatus;

public final class ResultPoints {

    private ResultPoints() {
    }

    public static int calculate(
            ResultStatus resultStatus,
            Integer finalPosition
    ) {
        if (resultStatus != ResultStatus.FINISHED
                || finalPosition == null) {
            return 0;
        }

        return switch (finalPosition) {
            case 1 -> 10;
            case 2 -> 7;
            case 3 -> 5;
            case 4 -> 3;
            case 5 -> 1;
            default -> 0;
        };
    }
}