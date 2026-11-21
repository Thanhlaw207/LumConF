package com.example.quanlysanbong

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Native view components shared by the PDF screens. No web view or screenshot layouts. */
open class SanNhaUi : AppCompatActivity() {
    val green = Color.rgb(20,133,73)
    val dark = Color.rgb(23,47,38)
    val muted = Color.rgb(114,131,123)
    val line = Color.rgb(221,229,224)
    val surface = Color.rgb(246,249,247)
    val mint = Color.rgb(234,246,238)
    val red = Color.rgb(190,67,63)
    val gold = Color.rgb(172,122,36)
    fun dp(n: Int) = (n * resources.displayMetrics.density + .5f).toInt()
    fun bg(color: Int, radius: Int = 14, border: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); border?.let { setStroke(dp(1),it) }
    }
    fun column(padding: Int = 0) = LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL; setPadding(dp(padding),dp(padding),dp(padding),dp(padding))
        layoutParams=LinearLayout.LayoutParams(-1,-2)
    }
    fun row() = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; layoutParams=LinearLayout.LayoutParams(-1,-2) }
    fun add(parent: ViewGroup, view: View, top: Int = 0, bottom: Int = 0) {
        val lp = (view.layoutParams as? LinearLayout.LayoutParams) ?: LinearLayout.LayoutParams(-1,-2)
        lp.topMargin=dp(top); lp.bottomMargin=dp(bottom); parent.addView(view,lp)
    }
    fun text(value: String, size: Int = 14, color: Int = dark, bold: Boolean = false) = TextView(this).apply {
        text=value; textSize=size.toFloat(); setTextColor(color); includeFontPadding=false
        typeface=Typeface.create(if(bold) "sans-serif-medium" else "sans-serif",if(bold) Typeface.BOLD else Typeface.NORMAL)
        setLineSpacing(dp(3).toFloat(),1f)
        layoutParams=LinearLayout.LayoutParams(-1,-2)
    }
    fun label(parent: ViewGroup, value: String, size: Int = 14, color: Int = dark, bold: Boolean = false, top: Int = 0): TextView =
        text(value,size,color,bold).also { add(parent,it,top) }
    fun heading(parent: ViewGroup, title: String, subtitle: String = "", back: (() -> Unit)? = null) {
        val r=row()
        if(back!=null) r.addView(icon("back",dark,22).apply { setOnClickListener { back() } },LinearLayout.LayoutParams(dp(32),dp(44)))
        r.addView(text(title,26,dark,true),LinearLayout.LayoutParams(0,-2,1f)); add(parent,r,8)
        if(subtitle.isNotBlank()) label(parent,subtitle,13,muted,top=8)
    }
    fun section(parent: ViewGroup, title: String, action: String = "", click: (() -> Unit)? = null) {
        val r=row(); r.addView(text(title,18,dark,true),LinearLayout.LayoutParams(0,-2,1f))
        if(action.isNotEmpty()) r.addView(text(action,12,green,true).apply { setPadding(dp(6),dp(12),0,dp(12)); setOnClickListener { click?.invoke() } },LinearLayout.LayoutParams(-2,dp(48)))
        add(parent,r,22,12)
    }
    fun card(parent: ViewGroup, color: Int = Color.WHITE, padding: Int = 16, top: Int = 12): LinearLayout = column(padding).also {
        it.background=bg(color,14,if(color==Color.WHITE) line else null); add(parent,it,top)
    }
    fun divider(parent: ViewGroup, top: Int = 12) { add(parent,View(this).apply { setBackgroundColor(line); layoutParams=LinearLayout.LayoutParams(-1,dp(1)) },top,top) }
    fun pair(parent: ViewGroup, left: String, right: String, strong: Boolean = false, color: Int = dark) {
        val r=row(); r.addView(text(left,13,muted),LinearLayout.LayoutParams(0,-2,1f))
        r.addView(text(right,if(strong) 17 else 13,color,strong).apply { gravity=Gravity.END },LinearLayout.LayoutParams(0,-2,1f)); add(parent,r,10)
    }
    fun notice(parent: ViewGroup, value: String, warning: Boolean = false, top: Int = 16) {
        val box=card(parent,if(warning) Color.rgb(255,246,227) else mint,14,top)
        val r=row(); r.addView(icon(if(warning) "info" else "shield",if(warning) gold else green,20),LinearLayout.LayoutParams(dp(28),dp(26)))
        r.addView(text(value,12,if(warning) gold else green),LinearLayout.LayoutParams(0,-2,1f)); box.addView(r)
    }
    fun button(parent: ViewGroup, value: String, primary: Boolean = true, danger: Boolean = false, top: Int = 14, click: () -> Unit): MaterialButton {
        val color=if(danger) red else green
        val b=MaterialButton(this).apply {
            text=value; isAllCaps=false; textSize=15f; typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL)
            cornerRadius=dp(12); insetTop=0; insetBottom=0
            setTextColor(if(primary) Color.WHITE else color)
            backgroundTintList=ColorStateList.valueOf(if(primary) color else Color.WHITE)
            strokeWidth=if(primary) 0 else dp(1); strokeColor=ColorStateList.valueOf(line)
            layoutParams=LinearLayout.LayoutParams(-1,dp(52)); setOnClickListener { click() }
        }; add(parent,b,top); return b
    }
    fun link(parent: ViewGroup, value: String, click: () -> Unit) = label(parent,value,14,green,top=10).apply {
        gravity=Gravity.CENTER; minHeight=dp(48); setOnClickListener { click() }
    }
    fun input(parent: ViewGroup, title: String, hint: String, value: String = "", type: Int = InputType.TYPE_CLASS_TEXT, multiline: Boolean = false): TextInputEditText {
        if(title.isNotEmpty()) label(parent,title,14,dark,true,18)
        val box=TextInputLayout(this).apply {
            isHintEnabled=false; boxBackgroundMode=TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor=surface; boxStrokeColor=green; setBoxStrokeColorStateList(ColorStateList(arrayOf(intArrayOf(android.R.attr.state_focused),intArrayOf()),intArrayOf(green,line)))
            setBoxCornerRadii(dp(13).toFloat(),dp(13).toFloat(),dp(13).toFloat(),dp(13).toFloat())
            if(type and InputType.TYPE_TEXT_VARIATION_PASSWORD != 0 && type and InputType.TYPE_CLASS_NUMBER == 0) endIconMode=TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }
        val edit=TextInputEditText(box.context).apply {
            id=("sn:$title:$hint".hashCode() and 0x00ffffff).coerceAtLeast(1)
            this.hint=hint; setText(value); setTextColor(dark); setHintTextColor(muted); textSize=15f
            inputType=type; setPadding(dp(16),dp(16),dp(16),dp(16))
            isSingleLine=!multiline; minHeight=dp(if(multiline) 92 else 54)
            if(multiline) { inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE; gravity=Gravity.TOP; maxLines=4 }
        }; box.addView(edit,LinearLayout.LayoutParams(-1,-2)); add(parent,box,8); return edit
    }
    fun chips(parent: ViewGroup, values: List<String>, selected: String, single: Boolean = true, click: (String)->Unit): ChipGroup {
        val g=ChipGroup(this).apply { isSingleSelection=single; isSelectionRequired=single; chipSpacingHorizontal=dp(6); chipSpacingVertical=dp(4) }
        values.forEach { value ->
            g.addView(Chip(this).apply {
                id=View.generateViewId(); text=value; textSize=12f; isCheckable=true; isChecked=value==selected; minHeight=dp(44)
                setEnsureMinTouchTargetSize(true)
                chipBackgroundColor=ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked),intArrayOf()),intArrayOf(mint,Color.WHITE))
                setTextColor(ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked),intArrayOf()),intArrayOf(green,muted)))
                chipStrokeColor=ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked),intArrayOf()),intArrayOf(green,line)); chipStrokeWidth=dp(1).toFloat()
                shapeAppearanceModel=shapeAppearanceModel.toBuilder().setAllCornerSizes(dp(8).toFloat()).build(); setOnClickListener { click(value) }
            })
        }; add(parent,g,10); return g
    }
    fun check(parent: ViewGroup, value: String, checked: Boolean = false) = CheckBox(this).apply {
        text=value; textSize=12f; setTextColor(muted); isChecked=checked; buttonTintList=ColorStateList.valueOf(green)
        minHeight=dp(48); add(parent,this,12)
    }
    fun image(parent: ViewGroup, resource: Int, height: Int) = ImageView(this).apply {
        setImageResource(resource); scaleType=ImageView.ScaleType.CENTER_CROP; background=bg(surface,12); clipToOutline=true
        layoutParams=LinearLayout.LayoutParams(-1,dp(height)); add(parent,this)
    }
    fun icon(name: String, color: Int = green, size: Int = 24) = SnIcon(this,name,color).apply {
        layoutParams=LinearLayout.LayoutParams(dp(size),dp(size)); contentDescription=name
    }
    fun menuRow(parent: ViewGroup, title: String, subtitle: String = "", name: String = "calendar", click: () -> Unit) {
        val r=row().apply { setPadding(0,dp(12),0,dp(12)); minimumHeight=dp(60); setOnClickListener { click() } }
        r.addView(icon(name),LinearLayout.LayoutParams(dp(32),dp(26)))
        val c=column(); label(c,title,14,dark,true); if(subtitle.isNotEmpty()) label(c,subtitle,11,muted,top=5)
        r.addView(c,LinearLayout.LayoutParams(0,-2,1f)); r.addView(icon("next",muted,18)); add(parent,r)
    }
    fun toggle(parent: ViewGroup, title: String, subtitle: String, checked: Boolean, change: (Boolean)->Unit) {
        val r=row(); val c=column(); label(c,title,14,dark,true); label(c,subtitle,11,muted,top=5)
        r.addView(c,LinearLayout.LayoutParams(0,-2,1f)); r.addView(Switch(this).apply { isChecked=checked; thumbTintList=ColorStateList.valueOf(green); contentDescription=title; setOnCheckedChangeListener { _,v -> change(v) } }); add(parent,r,14)
    }
}

/** Consistent outline icons, drawn in a 24-unit coordinate system. */
class SnIcon(context: Context, private val name: String, private val color: Int) : View(context) {
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s=minOf(width,height)/24f; canvas.save(); canvas.translate((width-24*s)/2,(height-24*s)/2); canvas.scale(s,s)
        val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=this@SnIcon.color; style=Paint.Style.STROKE; strokeWidth=1.7f; strokeCap=Paint.Cap.ROUND; strokeJoin=Paint.Join.ROUND }
        fun lines(vararg pts: Float) { val path=Path(); path.moveTo(pts[0],pts[1]); for(i in 2 until pts.size step 2) path.lineTo(pts[i],pts[i+1]); canvas.drawPath(path,p) }
        fun circle(x: Float,y: Float,r: Float) = canvas.drawCircle(x,y,r,p)
        fun rect(l: Float,t: Float,r: Float,b: Float,radius: Float=2f) = canvas.drawRoundRect(l,t,r,b,radius,radius,p)
        when(name) {
            "pitch" -> { rect(3f,2f,21f,22f); lines(3f,12f,21f,12f); circle(12f,12f,4f); rect(7f,2f,17f,6f,0f); rect(7f,18f,17f,22f,0f) }
            "home" -> { lines(3f,10f,12f,3f,21f,10f,21f,21f,3f,21f,3f,10f); rect(9f,13f,15f,21f,0f) }
            "calendar" -> { rect(3f,5f,21f,21f); lines(3f,10f,21f,10f); lines(7f,2f,7f,7f); lines(17f,2f,17f,7f); lines(8f,16f,11f,18f,16f,13f) }
            "heart" -> { val path=Path(); path.moveTo(12f,21f); path.cubicTo(-7f,7f,9f,-2f,12f,7f); path.cubicTo(17f,-3f,32f,8f,12f,21f); canvas.drawPath(path,p) }
            "user" -> { circle(12f,7f,4f); val path=Path(); path.moveTo(4f,22f); path.cubicTo(4f,10f,20f,10f,20f,22f); canvas.drawPath(path,p) }
            "search" -> { circle(10f,10f,7f); lines(15f,15f,22f,22f) }
            "back" -> { lines(14f,5f,7f,12f,14f,19f); lines(7f,12f,22f,12f) }
            "next" -> lines(9f,5f,16f,12f,9f,19f)
            "check" -> lines(4f,12f,9f,17f,20f,6f)
            "plus" -> { lines(12f,4f,12f,20f); lines(4f,12f,20f,12f) }
            "bell" -> { val path=Path(); path.moveTo(4f,18f); path.lineTo(6f,14f); path.lineTo(6f,9f); path.cubicTo(6f,1f,18f,1f,18f,9f); path.lineTo(18f,14f); path.lineTo(20f,18f); path.close(); canvas.drawPath(path,p); lines(10f,22f,14f,22f) }
            "pin" -> { val path=Path(); path.moveTo(12f,22f); path.cubicTo(-8f,2f,32f,2f,12f,22f); canvas.drawPath(path,p); circle(12f,9f,2.5f) }
            "clock" -> { circle(12f,12f,9f); lines(12f,6f,12f,12f,17f,14f) }
            "shield" -> { lines(12f,2f,20f,6f,19f,15f,12f,22f,5f,15f,4f,6f,12f,2f); lines(8f,11f,11f,14f,16f,9f) }
            "info" -> { circle(12f,12f,9f); lines(12f,11f,12f,17f); circle(12f,7f,.5f) }
            "lock" -> { rect(4f,10f,20f,22f); val path=Path(); path.moveTo(7f,10f); path.lineTo(7f,6f); path.cubicTo(7f,0f,17f,0f,17f,6f); path.lineTo(17f,10f); canvas.drawPath(path,p); circle(12f,16f,1f) }
            "chart" -> { lines(3f,3f,3f,21f,22f,21f); lines(6f,16f,11f,10f,15f,13f,21f,5f) }
            "phone" -> { lines(4f,3f,8f,3f,10f,8f,7f,11f,13f,17f,16f,14f,21f,16f,21f,20f); val path=Path(); path.moveTo(21f,20f); path.cubicTo(9f,25f,-2f,10f,4f,3f); canvas.drawPath(path,p) }
            "ticket" -> { rect(2f,5f,22f,19f); lines(7f,15f,17f,9f); circle(8f,9f,1f); circle(16f,15f,1f) }
            "star" -> { val path=Path(); for(i in 0..9) { val a=(i*36-90)*Math.PI/180; val r=if(i%2==0) 10 else 4; val x=12+(Math.cos(a)*r).toFloat(); val y=12+(Math.sin(a)*r).toFloat(); if(i==0) path.moveTo(x,y) else path.lineTo(x,y) }; path.close(); canvas.drawPath(path,p) }
            "edit" -> { lines(4f,16f,16f,4f,20f,8f,8f,20f,3f,21f,4f,16f); lines(13f,7f,17f,11f) }
            "wallet" -> { rect(2f,5f,22f,21f); rect(15f,10f,22f,16f); lines(3f,5f,18f,2f,18f,5f) }
            else -> { circle(12f,12f,9f); lines(8f,12f,16f,12f) }
        }; canvas.restore()
    }
}
