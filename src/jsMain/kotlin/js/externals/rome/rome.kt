/*
 * Arquivo que define interfaces externas com a biblioteca Rome.
 *
 * Essa interface não é completa e representa apenas a parte que é importante para esse projeto.
 */
package js.externals.rome

import moment.Moment
import org.w3c.dom.Element
import kotlin.js.Date

external val rome: RomeStatic

external interface Rome {
    fun show()

    fun hide()

    fun getMoment(): Moment?

    fun setValue(value: dynamic)

    fun getDate(): Date?

    fun destroy()

    fun restore(options: RomeOptions = definedExternally)

    fun options(options: RomeOptions = definedExternally)
}

external interface RomeOptions {
    /**
     * DOM element where the calendar will be appended to. Takes 'parent' as the parent element
     */
    var appendTo: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * When set to true, the calendar is auto-closed when picking a day (or a time if time: true and date: false).
     * A value of 'time' will only auto-close the calendar when a time is picked.
     */
    var autoClose: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Hides the calendar when focusing something other than the input field
     */
    var autoHideOnBlur: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Hides the calendar when clicking away
     */
    var autoHideOnClick: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * The calendar shows days and allows you to navigate between months
     */
    var date: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Function to validate that a given date is considered valid. Receives a native Date parameter.
     */
    var dateValidator: ((Date) -> Boolean)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Format string used to display days on the calendar
     */
    var dayFormat: String?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Value used to initialize calendar. Takes string, Date, or moment
     */
    var initialValue: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Format string used for the input field as well as the results of rome
     */
    var inputFormat: String?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Ensures the date is valid when the field is blurred
     */
    var invalidate: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Compares input strictly against inputFormat, and partial matches are discarded
     */
    var strictParse: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Disallow dates past max. Takes string, Date, or moment
     */
    var max: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Disallow dates before min. Takes string, Date, or moment
     */
    var min: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Format string used by the calendar to display months and their year
     */
    var monthFormat: String?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * How many months get rendered in the calendar
     */
    var monthsInCalendar: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Is the field required or do you allow empty values?
     */
    var required: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * CSS classes applied to elements on the calendar
     */
    var styles: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * The calendar shows the current time and allows you to change it using a dropdown
     */
    var time: Boolean?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Format string used to display the time on the calendar
     */
    var timeFormat: String?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Seconds between each option in the time dropdown
     */
    var timeInterval: Int?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Function to validate that a given time is considered valid. Receives a native Date parameter.
     */
    var timeValidator: ((Date) -> Boolean)?
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Format used to display weekdays. Takes min (Mo), short (Mon), long (Monday), or an array with seven strings of your choosing.
     */
    var weekdayFormat: dynamic
        get() = definedExternally
        set(value) = definedExternally

    /**
     * Day considered the first of the week. Range: Sunday 0-Saturday 6
     */
    var weekStart: Int?
        get() = definedExternally
        set(value) = definedExternally
}

fun romeOptions(block: RomeOptions.() -> Unit = {}): RomeOptions {
    return js("({})").unsafeCast<RomeOptions>().apply(block)
}

external interface RomeStatic {
    fun find(element: Element): Rome?
}

external fun rome(element: Element, options: RomeOptions = definedExternally): Rome