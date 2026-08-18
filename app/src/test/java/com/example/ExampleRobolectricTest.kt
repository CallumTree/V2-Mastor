package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.calculation.MastorCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mastor", appName)
  }

  @Test
  fun `verify calculation engine base rate and central uplifts`() {
    val baseCost = MastorCalculationEngine.roundMoney(10.0 * 1850.0) // 18,500.00
    val claimedBase = MastorCalculationEngine.roundMoney(baseCost * (0.50)) // 9,250.00
    val (u1, u2, total) = MastorCalculationEngine.calculateProjectUplifts(
        baseAmount = claimedBase,
        uplift1Percent = 15.0,
        uplift2Percent = 5.0
    )

    assertEquals(18500.00, baseCost, 0.01)
    assertEquals(9250.00, claimedBase, 0.01)
    assertEquals(1387.50, u1, 0.01) // 15% of 9250
    assertEquals(531.88, u2, 0.01)  // 5% of (9250 + 1387.50 = 10637.50)
    assertEquals(11169.38, total, 0.01)
  }
}
