package processing.parsingRules;

import static processing.parsingRules.IparsingRule.*;

/**
 * a factory for IparsingRule objets
 */
public class ParsingRuleFactory {

    public static final String EXCEPTION_NOT_ADDED_TO_FACTORY_MSG =
            " It Seems As if the new ParsingRule was not added to the SwitchCase";

    /**Returns wanted ParsingRule.
     * @param wantedRulesName the name must be exactly the enum that represents the wanted ParsingRule.
     * @return Notice the two reasons that this method can throw IllegalArgumentException.
     *      1. the wanted enum does not exist (and hopefully their for the wanted Paring Rule does not exist)
     *      2. the wanted enum exists but was not updated here in the factory.
     *      ( basically the enum system seems cool but not easily extendable )
     */
    public static IparsingRule StringToRule(String wantedRulesName ) {


        ParserTypes wantedEnum = ParserTypes.valueOf(wantedRulesName);
        IparsingRule newRule;

        switch (wantedEnum) {

            case SIMPLE:
                newRule = new SimpleParsingRule();
                break;

            case ST_TV:
                newRule = new STtvSeriesParsingRule();
                break;

            case ST_MOVIE:
                newRule = new STmovieParsingRule();
                break;

            default:
                throw new IllegalArgumentException(
                        EXCEPTION_NOT_ADDED_TO_FACTORY_MSG);

        }
        return newRule;
    }





}
