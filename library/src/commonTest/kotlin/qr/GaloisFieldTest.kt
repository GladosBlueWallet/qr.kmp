package qr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GaloisFieldTest {

    @Test
    fun exp_values() {
        assertEquals(1, GaloisField.exp(0))
        assertEquals(2, GaloisField.exp(1))
        assertEquals(4, GaloisField.exp(2))
        assertEquals(8, GaloisField.exp(3))
    }

    @Test
    fun log_values() {
        assertEquals(0, GaloisField.log(1))
        assertEquals(1, GaloisField.log(2))
        assertEquals(2, GaloisField.log(4))
        assertEquals(3, GaloisField.log(8))
    }

    @Test
    fun log_of_zero_throws() {
        assertFailsWith<IllegalArgumentException> { GaloisField.log(0) }
    }

    @Test
    fun mul_products() {
        assertEquals(0, GaloisField.mul(0, 5))
        assertEquals(0, GaloisField.mul(5, 0))
        assertEquals(6, GaloisField.mul(2, 3))
    }

    @Test
    fun add_xors() {
        assertEquals(0, GaloisField.add(5, 5))
        assertEquals(7, GaloisField.add(5, 2))
        assertEquals(6, GaloisField.add(5, 3))
    }

    @Test
    fun inv_is_multiplicative_inverse() {
        for (x in 1..255) {
            assertEquals(1, GaloisField.mul(x, GaloisField.inv(x)), "Failed for x=$x")
        }
    }

    @Test
    fun inv_of_zero_throws() {
        assertFailsWith<IllegalArgumentException> { GaloisField.inv(0) }
    }

    @Test
    fun polynomial_strips_leading_zeros() {
        assertContentEquals(intArrayOf(1, 2, 3), GaloisField.polynomial(intArrayOf(0, 0, 1, 2, 3)))
        assertContentEquals(intArrayOf(1, 2, 3), GaloisField.polynomial(intArrayOf(1, 2, 3)))
        assertContentEquals(intArrayOf(0), GaloisField.polynomial(intArrayOf(0, 0, 0)))
    }

    @Test
    fun monomial() {
        assertContentEquals(intArrayOf(5, 0, 0), GaloisField.monomial(2, 5))
        assertContentEquals(intArrayOf(1), GaloisField.monomial(0, 1))
        assertContentEquals(intArrayOf(0), GaloisField.monomial(5, 0))
    }

    @Test
    fun divisorPoly_degree() {
        assertEquals(11, GaloisField.divisorPoly(10).size)
    }

    @Test
    fun mulPoly() {
        val result = GaloisField.mulPoly(intArrayOf(1, 2), intArrayOf(1, 3))
        assertEquals(3, result.size)
        assertEquals(1, result[0])
    }

    @Test
    fun addPoly() {
        val result = GaloisField.addPoly(intArrayOf(1, 2, 3), intArrayOf(4, 5, 6))
        assertEquals(3, result.size)
        assertEquals(5, result[0])
        assertEquals(7, result[1])
        assertEquals(5, result[2])
    }
}
